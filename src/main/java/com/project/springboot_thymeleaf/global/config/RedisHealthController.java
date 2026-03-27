package com.project.springboot_thymeleaf.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 대시보드 + 연결 상태 확인 컨트롤러
 * GET    /redis/dashboard   → 대시보드 페이지
 * GET    /redis/ping        → PING/PONG 확인 (JSON)
 * GET    /redis/test        → 저장/조회/삭제 테스트 (JSON)
 * GET    /redis/api/keys    → 전체 키 목록 (JSON)
 * GET    /redis/api/value   → 특정 키 값 조회 (JSON)
 * POST   /redis/api/key     → 키 추가 (JSON)
 * PUT    /redis/api/key     → 키 수정 (JSON)
 * DELETE /redis/api/key     → 특정 키 삭제 (JSON)
 */
@Slf4j
@Controller
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisHealthController {

    private final RedisTemplate<String, Object> redisTemplate;

    // ──────────────────────────────────────────────
    // 대시보드 페이지
    // ──────────────────────────────────────────────

    /** GET /redis/dashboard */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        boolean connected = false;
        String pingResult = "FAIL";
        Properties info = new Properties();
        long dbSize = 0;

        try (RedisConnection connection = Objects.requireNonNull(
                redisTemplate.getConnectionFactory(), "RedisConnectionFactory is null").getConnection()) {

            pingResult = Optional.ofNullable(connection.ping()).orElse("N/A");
            connected = "PONG".equalsIgnoreCase(pingResult);

            // Redis INFO는 Properties 형태로 받는 것이 가장 안전합니다.
            Properties serverInfo = connection.serverCommands().info("server");
            Properties memoryInfo = connection.serverCommands().info("memory");
            Properties clientInfo = connection.serverCommands().info("clients");

            if (serverInfo != null) info.putAll(serverInfo);
            if (memoryInfo != null) info.putAll(memoryInfo);
            if (clientInfo != null) info.putAll(clientInfo);

            Long size = connection.serverCommands().dbSize();
            dbSize = size != null ? size : 0L;
        } catch (Exception e) {
            log.error("[Redis Dashboard] 연결 오류 - {}", e.getMessage());
        }

        // 전체 키 목록
        Set<String> keys = redisTemplate.keys("*");
        List<Map<String, Object>> keyList = new ArrayList<>();
        List<String> sortedKeys = new ArrayList<>(keys != null ? keys : Collections.emptySet());

        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("key", key);
            try {
                String type = Objects.requireNonNull(redisTemplate.type(key)).code();
                Long   ttl  = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                String displayValue;
                switch (type) {
                    case "string" -> {
                        Object v = redisTemplate.opsForValue().get(key);
                        displayValue = v != null ? v.toString() : "(null)";
                    }
                    case "hash" -> {
                        Map<Object, Object> h = redisTemplate.opsForHash().entries(key);
                        displayValue = h.toString();
                    }
                    case "list" -> {
                        List<Object> l = redisTemplate.opsForList().range(key, 0, -1);
                        displayValue = l != null ? l.toString() : "[]";
                    }
                    case "set" -> {
                        Set<Object> s = redisTemplate.opsForSet().members(key);
                        displayValue = s != null ? s.toString() : "{}";
                    }
                    case "zset" -> {
                        Set<Object> z = redisTemplate.opsForZSet().range(key, 0, -1);
                        displayValue = z != null ? z.toString() : "{}";
                    }
                    default -> displayValue = "(조회 불가)";
                }
                entry.put("type",  type);
                entry.put("ttl", ttl >= 0 ? ttl + "s" : "영구");
                entry.put("value", displayValue);
            } catch (Exception e) {
                entry.put("type",  "unknown");
                entry.put("ttl",   "-");
                entry.put("value", "-");
            }
            keyList.add(entry);
        }

        model.addAttribute("connected", connected);
        model.addAttribute("pingResult", pingResult);
        model.addAttribute("dbSize", dbSize);
        model.addAttribute("redisVersion", info.getProperty("redis_version", "N/A"));
        model.addAttribute("usedMemory", info.getProperty("used_memory_human", "N/A"));
        model.addAttribute("connectedClients", info.getProperty("connected_clients", "N/A"));
        model.addAttribute("uptimeInDays", info.getProperty("uptime_in_days", "N/A"));
        model.addAttribute("keyList", keyList);
        return "redis/dashboard";
    }

    // ──────────────────────────────────────────────
    // REST API (JSON)
    // ──────────────────────────────────────────────

    /** GET /redis/ping */
    @ResponseBody
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String pong = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection().ping();
            result.put("status",   "OK");
            result.put("response", pong);
            log.info("[Redis] ping 성공 - response: {}", pong);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error",  e.getMessage());
            log.error("[Redis] ping 실패 - {}", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /** GET /redis/test */
    @ResponseBody
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> result = new LinkedHashMap<>();
        String testKey   = "redis:health:test";
        String testValue = "hello-redis";
        try {
            redisTemplate.opsForValue().set(testKey, testValue);
            result.put("set", "OK");

            Object saved = redisTemplate.opsForValue().get(testKey);
            result.put("get",   saved);
            result.put("match", testValue.equals(String.valueOf(saved)));

            redisTemplate.delete(testKey);
            result.put("delete", "OK");
            result.put("status", "OK");
            log.info("[Redis] 저장/조회/삭제 테스트 성공");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error",  e.getMessage());
            log.error("[Redis] 테스트 실패 - {}", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /** GET /redis/api/keys?pattern=* */
    @ResponseBody
    @GetMapping("/api/keys")
    public ResponseEntity<Map<String, Object>> getKeys(
            @RequestParam(defaultValue = "*") String pattern) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Set<String> safeKeys = redisTemplate.keys(pattern);
            result.put("status", "OK");
            result.put("count", safeKeys.size());
            result.put("keys", safeKeys);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error",  e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /** GET /redis/api/value?key=xxx */
    @ResponseBody
    @GetMapping("/api/value")
    public ResponseEntity<Map<String, Object>> getValue(@RequestParam String key) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String type = Objects.requireNonNullElse(redisTemplate.type(key), org.springframework.data.redis.connection.DataType.NONE).code();
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            Object value;
            switch (type) {
                case "string" -> value = redisTemplate.opsForValue().get(key);
                case "hash" -> value = redisTemplate.opsForHash().entries(key);
                case "list" -> value = redisTemplate.opsForList().range(key, 0, -1);
                case "set" -> value = redisTemplate.opsForSet().members(key);
                case "zset" -> value = redisTemplate.opsForZSet().range(key, 0, -1);
                default -> value = null;
            }

            result.put("status", "OK");
            result.put("key", key);
            result.put("type", type);
            result.put("value", value);
            result.put("ttl", ttl != null && ttl >= 0 ? ttl + "s" : "영구");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error",  e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /** DELETE /redis/api/key?key=xxx */
    @ResponseBody
    @DeleteMapping("/api/key")
    public ResponseEntity<Map<String, Object>> deleteKey(@RequestParam String key) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Boolean deleted = redisTemplate.delete(key);
            result.put("status",  "OK");
            result.put("key",     key);
            result.put("deleted", deleted);
            log.info("[Redis Dashboard] 키 삭제 - key: {}", key);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error",  e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /**
     * POST /redis/api/key
     * body: { "key": "...", "value": "...", "ttl": 3600 }  ← ttl 단위: 초, 0이면 영구
     */
    @ResponseBody
    @PostMapping("/api/key")
    public ResponseEntity<Map<String, Object>> addKey(@RequestBody Map<String, Object> body) {
        return saveKey(body, false);
    }

    /**
     * PUT /redis/api/key
     * body: { "key": "...", "value": "...", "ttl": 3600 }
     */
    @ResponseBody
    @PutMapping("/api/key")
    public ResponseEntity<Map<String, Object>> updateKey(@RequestBody Map<String, Object> body) {
        return saveKey(body, true);
    }

    private ResponseEntity<Map<String, Object>> saveKey(Map<String, Object> body, boolean isUpdate) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String key   = String.valueOf(body.getOrDefault("key",   ""));
            String value = String.valueOf(body.getOrDefault("value", ""));
            long   ttl   = Long.parseLong(String.valueOf(body.getOrDefault("ttl", "0")));

            if (key.isBlank()) {
                result.put("status", "FAIL");
                result.put("error",  "key는 필수입니다.");
                return ResponseEntity.badRequest().body(result);
            }
            if (isUpdate && !redisTemplate.hasKey(key)) {
                result.put("status", "FAIL");
                result.put("error",  "수정할 키가 존재하지 않습니다: " + key);
                return ResponseEntity.badRequest().body(result);
            }

            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }

            result.put("status", "OK");
            result.put("key",    key);
            result.put("value",  value);
            result.put("ttl",    ttl > 0 ? ttl + "s" : "영구");
            log.info("[Redis Dashboard] 키 {} - key: {}", isUpdate ? "수정" : "추가", key);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error",  e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /** GET /redis/api/entries?pattern=* */
    @ResponseBody
    @GetMapping("/api/entries")
    public ResponseEntity<Map<String, Object>> getEntries(
            @RequestParam(defaultValue = "*") String pattern) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            List<String> sortedKeys = new ArrayList<>(keys);
            Collections.sort(sortedKeys);

            List<Map<String, Object>> entries = new ArrayList<>();
            for (String key : sortedKeys) {
                entries.add(buildEntry(key));
            }

            result.put("status", "OK");
            result.put("count", entries.size());
            result.put("entries", entries);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("error", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    private Map<String, Object> buildEntry(String key) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("key", key);
        try {
            String type = Objects.requireNonNullElse(redisTemplate.type(key), org.springframework.data.redis.connection.DataType.NONE).code();
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            Object value;
            switch (type) {
                case "string" -> value = redisTemplate.opsForValue().get(key);
                case "hash" -> value = redisTemplate.opsForHash().entries(key);
                case "list" -> value = redisTemplate.opsForList().range(key, 0, -1);
                case "set" -> value = redisTemplate.opsForSet().members(key);
                case "zset" -> value = redisTemplate.opsForZSet().range(key, 0, -1);
                default -> value = null;
            }

            entry.put("type", type);
            entry.put("ttl", ttl >= 0 ? ttl + "s" : "영구");
            entry.put("value", value != null ? value.toString() : "");
        } catch (Exception e) {
            entry.put("type", "unknown");
            entry.put("ttl", "-");
            entry.put("value", "-");
        }
        return entry;
    }
}
