# 🎡 룰렛 이벤트 API 명세

## 개요

| 항목 | 내용 |
|---|---|
| Base URL | `/api/roulette` |
| 컨트롤러 | `biz/roulette/web/RouletteApiController.java` |
| 서비스 구현체 | `biz/roulette/service/impl/RouletteServiceImpl.java` |
| 현재 상태 | 외부 CXM 연동 전 — 스텁(stub) 응답 반환 |

> **TODO**: 실제 서비스 연동 시 `RouletteServiceImpl` 내부의 스텁 코드를 `WebClient` 호출로 교체합니다.

---

## 엔드포인트 목록

| IF | Method | 내부 경로 | 외부 CXM 경로 | 설명 |
|---|---|---|---|---|
| IF-CX-047 | POST | `/api/roulette/event-list` | `POST /api/v1/evm/ec-event-list` | 이벤트 정보 조회 |
| IF-CX-049 | POST | `/api/roulette/prize-list` | `POST /api/v1/evm/ec-event-prize-list` | 이벤트 혜택 구성 정보 조회 |
| IF-CX-050 | GET  | `/api/roulette/raffle-info` | `GET /api/v1/evm/ec-event-raffle-info` | 응모권 횟수 조회 |
| IF-CX-051 | POST | `/api/roulette/draw` | `POST /api/v1/evm/ec-event-draw` | 당첨자 선정 (룰렛 스핀) |
| IF-CX-052 | POST | `/api/roulette/target-check` | `POST /api/v1/evm/ec-event-target-check` | 이벤트 신청대상 확인 |

---

## API 상세

### [IF-CX-047] 이벤트 정보 조회

- **Method/Path**: `POST /api/roulette/event-list`
- **Request Body**: 없음

**예시 요청**
```bash
curl -X POST 'http://localhost:8080/api/roulette/event-list'
```

**예시 응답**
```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "ecEventList": [
      {
        "eventId": 1,
        "eventName": "봄맞이 룰렛 이벤트",
        "eventStartDate": "20260301",
        "eventEndDate": "20260430"
      }
    ]
  }
}
```

---

### [IF-CX-049] 이벤트 혜택 구성 정보 조회

- **Method/Path**: `POST /api/roulette/prize-list`
- **Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `eventId` | Long | ✅ | 이벤트 아이디 |

**예시 요청**
```bash
curl -X POST 'http://localhost:8080/api/roulette/prize-list' \
  -H 'Content-Type: application/json' \
  -d '{ "eventId": 1 }'
```

**예시 응답**
```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "ecEventPrizeList": [
      { "couponName": "10% 할인 쿠폰", "couponCode": "DC10",       "winningRate": 20 },
      { "couponName": "무료 배송",      "couponCode": "FREE_SHIP",  "winningRate": 20 },
      { "couponName": "5,000 포인트",   "couponCode": "PT5000",     "winningRate": 15 },
      { "couponName": "20% 할인 쿠폰",  "couponCode": "DC20",       "winningRate": 10 },
      { "couponName": "1,000 포인트",   "couponCode": "PT1000",     "winningRate": 15 },
      { "couponName": "꽝",             "couponCode": "NONE",       "winningRate": 20 }
    ]
  }
}
```

---

### [IF-CX-050] 응모권 횟수 조회

- **Method/Path**: `GET /api/roulette/raffle-info`
- **Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `eventId` | Long | ✅ | 이벤트 아이디 |
| `acntNo` | String(10) | ✅ | 전자카드 고객번호 |

**예시 요청**
```bash
curl -X GET 'http://localhost:8080/api/roulette/raffle-info?eventId=1&acntNo=1234567890'
```

**예시 응답**
```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "ecEventRaffleInfo": [
      {
        "raffleTicketCount": 3
      }
    ]
  }
}
```

---

### [IF-CX-051] 당첨자 선정 (룰렛 스핀)

- **Method/Path**: `POST /api/roulette/draw`
- **Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `eventId` | Long | ✅ | 이벤트 아이디 |
| `acntNo` | String(10) | ✅ | 전자카드 고객번호 |

**예시 요청**
```bash
curl -X POST 'http://localhost:8080/api/roulette/draw' \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": 1,
    "acntNo": "1234567890"
  }'
```

**예시 응답 — 당첨**
```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "ecEventDraw": [
      {
        "isWinning": "1",
        "winningCouponName": "10% 할인 쿠폰"
      }
    ]
  }
}
```

**예시 응답 — 꽝**
```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "ecEventDraw": [
      {
        "isWinning": "0",
        "winningCouponName": null
      }
    ]
  }
}
```

> `isWinning`: `"1"` = 당첨, `"0"` = 꽝  
> 스텁 모드에서는 경품 6종 중 랜덤 선택됩니다.

---

### [IF-CX-052] 이벤트 신청대상 확인

- **Method/Path**: `POST /api/roulette/target-check`
- **Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `eventId` | Long | ✅ | 이벤트 아이디 |
| `acntNo` | String(10) | ✅ | 전자카드 고객번호 |

**예시 요청**
```bash
curl -X POST 'http://localhost:8080/api/roulette/target-check' \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": 1,
    "acntNo": "1234567890"
  }'
```

**예시 응답**
```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "ecEventTargetCheck": [
      {
        "isTarget": "1",
        "targetCouponName": null
      }
    ]
  }
}
```

> `isTarget`: `"1"` = 대상, `"0"` = 미대상

---

## DTO 구조

```
biz/roulette/dto/
├── RouletteEventListResDto.java          # IF-CX-047 응답
├── RouletteEventPrizeListReqDto.java     # IF-CX-049 요청
├── RouletteEventPrizeListResDto.java     # IF-CX-049 응답
├── RouletteRaffleInfoReqDto.java         # IF-CX-050 요청
├── RouletteRaffleInfoResDto.java         # IF-CX-050 응답
├── RouletteDrawReqDto.java               # IF-CX-051 요청
├── RouletteDrawResDto.java               # IF-CX-051 응답
├── RouletteTargetCheckReqDto.java        # IF-CX-052 요청
└── RouletteTargetCheckResDto.java        # IF-CX-052 응답
```

---

## 공통 응답 구조

```json
{
  "resultCode": 200,
  "resultMsg": "정상 처리되었습니다.",
  "resultData": {
    "isSuccess": true,
    "errMsg": null,
    "<<데이터 필드>>": [ ... ]
  }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `resultCode` | Integer | HTTP 상태 유사 코드 |
| `resultMsg` | String | 결과 메시지 |
| `resultData.isSuccess` | Boolean | API 처리 성공 여부 |
| `resultData.errMsg` | String | 오류 시 에러 메시지 (성공 시 null) |

