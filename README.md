# 🚀 Spring Boot 4 & Vanilla JS Web Project

개인프로젝트

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.3.6
- **Language**: Java 17
- **Build Tool**: Gradle
- **Template Engine**: Thymeleaf
- **DB : Maria DB
- **Tool : DBeaver

### Frontend
- **Styling**: Vanilla JS

## 🖥 Development Environment
- **OS**: macOS Sequoia (M4 MacBook Air)
- **IDE**: IntelliJ IDEA (Ultimate/Community)

## 🎡 Roulette API

룰렛 이벤트 API(IF-CX-047 ~ IF-CX-052) 상세 명세는 별도 문서를 참고하세요.

📄 **[docs/README-ROULETTE-API.md](docs/README-ROULETTE-API.md)**

| IF | Method | 경로 | 설명 |
|---|---|---|---|
| IF-CX-047 | POST | `/api/roulette/event-list` | 이벤트 정보 조회 |
| IF-CX-049 | POST | `/api/roulette/prize-list` | 이벤트 혜택 구성 정보 조회 |
| IF-CX-050 | GET  | `/api/roulette/raffle-info` | 응모권 횟수 조회 |
| IF-CX-051 | POST | `/api/roulette/draw` | 당첨자 선정 (룰렛 스핀) |
| IF-CX-052 | POST | `/api/roulette/target-check` | 이벤트 신청대상 확인 |
