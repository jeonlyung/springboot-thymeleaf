# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Personal project: a Spring Boot 3.3.6 (Java 17) server-rendered app using Thymeleaf, MyBatis + MariaDB, Redis, and OAuth2/JWT auth. Package root: `com.project.springboot_thymeleaf`.

## Common commands

```bash
./gradlew bootRun                       # run the app (localhost:8080)
./gradlew build                         # full build (compiles, tests, packages jar)
./gradlew test                          # run all tests
./gradlew test --tests "RedisFailSafeTest"          # run a single test class
./gradlew test --tests "*.RedisFailSafeTest.dashboardRendersWithDefaultsWhenRedisIsUnavailable"  # single test method
./gradlew clean --refresh-dependencies  # nuke Gradle cache (used by fix-jwt-import.sh when IntelliJ can't resolve jjwt)
```

There is no separate lint task configured; rely on `test`/`build` to catch issues.

### Required environment/config before running locally

`application.yml` pulls secrets from env vars / system properties that have no defaults in-repo:
- `DB_ID`, `DB_PASSWORD` — MariaDB (`jdbc:mariadb://localhost:3306/dangdang_db`)
- `GOOGLE_CLIENT_ID`/`GOOGLE_SECRET_KEY`, `NAVER_CLIENT_ID`/`NAVER_SECRET_KEY`, `KAKAO_CLIENT_ID`/`KAKAO_SECRET_KEY` — OAuth2 login providers
- Redis expected at `localhost:6379` (no password by default)
- `-Djasypt.password=...` — decrypts any `ENC(...)` values in config (see `JasyptConfig`); falls back to `default_key` if unset

The app is designed to boot and serve pages even when Redis is down — see "Redis fail-safe" below.

## Architecture

### Package layout

- `biz/<feature>/` — vertical feature slices, each with `dto/`, `mapper/` (MyBatis interfaces), `service/` + `service/impl/`, `web/` (controllers). Current features: `login` (OAuth2 login/session) and `roulette` (event/roulette game).
- `global/` — cross-cutting concerns: `security` (Spring Security + JWT + Jasypt), `config`, `interceptor`, `exception`, `aop`, `sftp`, `batch`, `message`, `constatns` (sic — `Constants.java` lives under this misspelled package).

### Request flow / auth model

- Stateless-leaning JWT auth layered on top of Spring Security OAuth2 login (`SecurityConfig`): OAuth2 login (Google/Naver/Kakao) succeeds via `OAuth2AuthenticationSuccessHandler`, which issues JWTs; `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter` to authenticate subsequent requests from the JWT.
- `CustomOAuth2User` (in `biz/login/dto`) is the principal type; controllers pull the user via `@AuthenticationPrincipal Object principal` and cast to `CustomOAuth2User` to get the email/usrId (see `RouletteApiController.extractUsrId`).
- `SecurityConfig` permits `/`, `/login/**`, `/oauth2/**`, `/css|js|images/**`, `/redis/**`, `/actuator/**`; everything else requires authentication.
- `CustomInterceptor` adds a secondary check: any controller whose package contains `.api` (i.e. `biz/**/web` classes under an `api`-named package, or more precisely those matched by `packageName.contains(".api")`) must send a non-blank `Authorization` header or it 401s at the interceptor level — this is in addition to, not instead of, Spring Security.
- `RefreshTokenService` persists refresh tokens in Redis but swallows Redis errors (see below).

### Redis fail-safe pattern

Redis is treated as non-critical: if it's unreachable, features degrade instead of throwing. `RefreshTokenService.save/get` and `RedisHealthController.dashboard` catch Redis exceptions and return safe defaults (empty list, `dbSize=0`, `pingResult="FAIL"`) rather than propagating. When touching Redis-backed code, preserve this behavior — `RedisFailSafeTest` asserts on it directly by mocking `RedisConnectionFactory`/`RedisTemplate` to throw and checking the fallback values.

### MyBatis / DB layer

- Mapper XML lives in `src/main/resources/mapper/<feature>/*.xml`; Java mapper interfaces live in `biz/<feature>/mapper/`. `mybatis.mapper-locations` is `classpath:mapper/**/*.xml`, `map-underscore-to-camel-case: true`.
- `CustomMapperInterceptor` (MyBatis `Interceptor` on `StatementHandler.prepare`) logs every SQL statement's execution time; large SQL bodies (>1000 chars) log length only, not full text.
- DDL for the roulette feature is checked into `src/main/resources/mapper/ddl/` (`TB_RLT_SCHEMA.sql`, `TB_RLT_DML.sql`, migration scripts) — treat these as the source of truth for the roulette tables' shape when writing new mapper SQL.

### Roulette feature (most actively developed area)

Two parallel API surfaces exist, documented in detail in `docs/README-ROULETTE-API.md`:
- `RouletteApiController` at `/api/roulette` and `/api/v1/roulette` — the primary, currently-used API (init, raffle-info, raffle-join, draw, target-check). Backed by real MariaDB data via `RouletteMapper`/`RouletteServiceImpl` (no longer a stub for these endpoints).
- `RouletteEvmApiController` at `/api/v1/evm` — external "CXM" integration endpoints (IF-CX-047 event-list, IF-CX-049 prize-list), still stubbed pending real downstream integration.

Response shape convention for roulette DTOs: `{ resultCode, resultMsg, resultData: { isSuccess, errMsg, <listField>: [...] } }`. Boolean-ish result fields are transmitted as the strings `"1"`/`"0"` (e.g. `isWinning`, `isTarget`), not JSON booleans — follow this when adding new roulette response fields.

`RouletteApiController` resolves `eventId`/`usrId` from the authenticated principal when not supplied on the request body (`resolveRequestContext`, overloaded per DTO type — there's no shared interface for these req DTOs, hence the repeated overloads).

`RouletteServiceImpl.pickPrizeByWeight` implements weighted-random prize selection using each prize's `winningRate` as an integer weight; ticket decrement and draw-history insert happen inside the same `@Transactional` method (`drawEvent`) after target/ticket-balance checks.

### Frontend

Server-rendered Thymeleaf templates under `src/main/resources/templates/`, using `thymeleaf-layout-dialect` with a shared `common/layout.html` + `common/fragments/`. No frontend build step — plain JS in `static/js/` (`app.js`, `common.js`) and plain CSS per page in `static/css/`.
