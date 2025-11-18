# ELK Monitoring System

ELK Stack (Elasticsearch, Logstash, Kibana)을 활용한 장애 알림 및 로그 수집 자동화 시스템

## 프로젝트 개요

이 프로젝트는 Spring Boot 애플리케이션의 로그를 ELK 스택으로 수집하고, 실시간으로 모니터링하며, 장애 발생 시 알림을 보내는 시스템입니다.

### 주요 기능

- **구조화된 로깅**: JSON 형식의 구조화된 로그 생성
- **실시간 로그 수집**: Logstash를 통한 실시간 로그 전송
- **로그 저장 및 검색**: Elasticsearch에 로그 인덱싱 및 검색
- **시각화**: Kibana 대시보드를 통한 로그 시각화
- **알림**: 에러 발생 시 자동 알림
- **요청 추적**: MDC를 통한 HTTP 요청 추적
- **비즈니스 이벤트 로깅**: 비즈니스 이벤트 별도 추적

## 기술 스택

### Backend
- **Java**: 17+
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: 데이터 접근
- **MySQL**: 8.0
- **Gradle**: 8.5

### Logging
- **Logback**: 로깅 프레임워크
- **Logstash Logback Encoder**: JSON 로그 인코딩

### ELK Stack
- **Elasticsearch**: 8.11.0 - 로그 저장 및 검색
- **Logstash**: 8.11.0 - 로그 수집 및 변환
- **Kibana**: 8.11.0 - 로그 시각화

### Infrastructure
- **Docker & Docker Compose**: 컨테이너 오케스트레이션

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controller   │→ │   Service    │→ │  Repository  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         ↓                  ↓                  ↓              │
│  ┌──────────────────────────────────────────────────┐       │
│  │            Logback (JSON Encoder)                │       │
│  └──────────────────────────────────────────────────┘       │
│         ↓ (TCP)           ↓ (File)                          │
└─────────┼──────────────────┼──────────────────────────────┘
          ↓                  ↓
    ┌─────────────────────────────┐
    │        Logstash             │
    │  ┌─────────────────────┐    │
    │  │ Input (TCP + File)  │    │
    │  ├─────────────────────┤    │
    │  │ Filter (Parse/Tag)  │    │
    │  ├─────────────────────┤    │
    │  │ Output (ES + Debug) │    │
    │  └─────────────────────┘    │
    └──────────┬──────────────────┘
               ↓
    ┌─────────────────────────────┐
    │      Elasticsearch          │
    │  ┌─────────────────────┐    │
    │  │ Index: application- │    │
    │  │ logs-YYYY.MM.dd     │    │
    │  └─────────────────────┘    │
    └──────────┬──────────────────┘
               ↓
    ┌─────────────────────────────┐
    │         Kibana              │
    │  ┌─────────────────────┐    │
    │  │   Discover          │    │
    │  │   Visualizations    │    │
    │  │   Dashboards        │    │
    │  │   Alerting          │    │
    │  └─────────────────────┘    │
    └─────────────────────────────┘
```

## 프로젝트 구조

```
elk-monitoring-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/elkmonitoring/
│   │   │   ├── config/              # 설정 클래스
│   │   │   │   ├── LoggingInterceptor.java
│   │   │   │   ├── LoggingUtils.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/          # REST API 컨트롤러
│   │   │   │   ├── UserController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── service/             # 비즈니스 로직
│   │   │   │   └── UserService.java
│   │   │   ├── repository/          # 데이터 접근
│   │   │   │   └── UserRepository.java
│   │   │   ├── domain/              # 엔티티
│   │   │   │   └── User.java
│   │   │   ├── dto/                 # DTO
│   │   │   │   ├── UserRequest.java
│   │   │   │   └── UserResponse.java
│   │   │   ├── exception/           # 예외 처리
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   └── ElkMonitoringApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Spring Boot 설정
│   │       └── logback-spring.xml   # Logback 설정
│   └── test/
├── logstash/
│   ├── config/
│   │   └── logstash.yml             # Logstash 기본 설정
│   └── pipeline/
│       └── logstash.conf            # 로그 파이프라인 정의
├── logs/                            # 로그 파일 저장 위치
├── docker-compose.yml               # Docker Compose 설정
├── build.gradle                     # Gradle 빌드 설정
├── start.sh                         # 전체 시스템 시작 스크립트
├── generate-logs.sh                 # 로그 생성 테스트 스크립트
├── README.md                        # 이 파일
├── DOCKER_GUIDE.md                  # Docker 가이드
├── LOGGING_GUIDE.md                 # 로깅 가이드
├── API_EXAMPLES.md                  # API 사용 예제
└── KIBANA_GUIDE.md                  # Kibana 가이드
```

## 빠른 시작

### 사전 요구사항

- **Docker & Docker Compose**: 설치 필요
- **Java**: 17 이상
- **포트**: 3306, 5044, 8080, 9200, 9300, 5601, 9600 사용 가능
- **Slack Webhook URL** (선택): 에러 알림을 받으려면 필요

### 설치 및 실행

#### 1. 프로젝트 클론 또는 이동
```bash
cd elk-monitoring-system
```

#### 2. Slack 알림 설정 (선택)

ERROR 로그 발생 시 Slack 알림을 받으려면:

1. Slack Webhook URL 생성
   - https://api.slack.com/messaging/webhooks 접속
   - "Create your Slack app" 클릭
   - "Incoming Webhooks" 활성화
   - Webhook URL 복사

2. 환경 변수 설정
```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 편집하여 Webhook URL 입력
# SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
```

#### 3. Docker 서비스 시작
```bash
docker-compose up -d
```

#### 4. 서비스 확인
```bash
# Elasticsearch 상태 확인
curl http://localhost:9200/_cluster/health?pretty

# Kibana 상태 확인
curl http://localhost:5601/api/status
```

#### 5. Spring Boot 애플리케이션 실행
```bash
./gradlew bootRun
```

또는 통합 시작 스크립트 사용:
```bash
./start.sh
```

#### 6. API 테스트
```bash
# 헬스 체크
curl http://localhost:8080/health

# 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","phone":"010-1234-5678"}'

# 에러 로그 생성 (Slack 알림 테스트)
curl http://localhost:8080/api/users/test/error

# 로그 생성 스크립트 실행
./generate-logs.sh
```

#### 7. Kibana에서 로그 확인
브라우저에서 http://localhost:5601 접속

#### 8. Slack에서 알림 확인
ERROR 로그가 발생하면 Slack 채널에 다음과 같은 알림이 전송됩니다:
- Application 정보 (elk-monitoring-system 또는 shoppingmall)
- 에러 발생 시각
- Logger 이름
- 에러 메시지

## 주요 엔드포인트

### 애플리케이션
- **Spring Boot**: http://localhost:8080
- **Health Check**: http://localhost:8080/health

### ELK Stack
- **Elasticsearch**: http://localhost:9200
- **Logstash API**: http://localhost:9600
- **Kibana**: http://localhost:5601

### 데이터베이스
- **MySQL**: localhost:3306
  - Database: `elk_monitoring`
  - Username: `root`
  - Password: `root1234`

## API 문서

자세한 API 사용법은 [API_EXAMPLES.md](./API_EXAMPLES.md) 참조

### 주요 API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /health | 헬스 체크 |
| GET | /api/users | 모든 사용자 조회 |
| GET | /api/users/{id} | 특정 사용자 조회 |
| POST | /api/users | 사용자 생성 |
| PUT | /api/users/{id} | 사용자 수정 |
| DELETE | /api/users/{id} | 사용자 삭제 |
| PATCH | /api/users/{id}/status | 사용자 상태 변경 |
| GET | /api/users/test/error | 에러 시뮬레이션 |

## 로그 구조

### JSON 로그 예시

```json
{
  "@timestamp": "2025-11-07T10:00:00.000Z",
  "level": "INFO",
  "logger_name": "com.example.elkmonitoring.service.UserService",
  "message": "User created successfully",
  "thread_name": "http-nio-8080-exec-1",
  "app_name": "elk-monitoring-system",
  "environment": "development",
  "requestId": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "user123",
  "clientIp": "127.0.0.1",
  "event_type": "business_event",
  "event_name": "user_created",
  "user_id": 1,
  "user_email": "hong@example.com"
}
```

### 로그 레벨

- **DEBUG**: 개발 디버깅 정보
- **INFO**: 일반 정보성 메시지
- **WARN**: 경고성 메시지
- **ERROR**: 에러 메시지 (알림 대상)

### MDC 필드

- **requestId**: HTTP 요청 고유 ID (UUID)
- **userId**: 사용자 식별자
- **clientIp**: 클라이언트 IP 주소

## 모니터링 및 알림

### Slack 알림 (Logstash HTTP Output Plugin)

ERROR 레벨 로그 발생 시 실시간으로 Slack 알림이 전송됩니다.

**알림 내용:**
- 에러 발생 애플리케이션 (태그 기반)
- 에러 발생 시각
- Logger 이름
- 로그 레벨
- 에러 메시지

**장점:**
- 실시간 알림 (로그 발생 즉시)
- 설정이 간단함
- 애플리케이션 코드 수정 불필요

**주의사항:**
- 모든 ERROR 로그마다 알림이 전송됩니다
- 대량의 에러 발생 시 알림이 많을 수 있습니다
- 필요 시 filter에서 특정 조건으로 제한 가능

### Kibana 대시보드

1. http://localhost:5601 접속
2. Data View 생성: `application-logs-*`
3. 시각화 및 대시보드 구성

자세한 내용은 [KIBANA_GUIDE.md](./KIBANA_GUIDE.md) 참조

## 학습 포인트

### 1. ELK Stack 이해
- Elasticsearch: 로그 저장 및 검색 엔진
- Logstash: 로그 수집 및 변환 파이프라인
- Kibana: 로그 시각화 및 대시보드

### 2. 구조화된 로깅
- JSON 형식의 로그 생성
- Logstash Encoder 사용
- 구조화된 필드를 통한 효율적인 검색

### 3. MDC (Mapped Diagnostic Context)
- 요청별 컨텍스트 정보 추적
- 분산 시스템에서의 요청 추적

### 4. 비즈니스 이벤트 로깅
- 비즈니스 로직 실행 추적
- 이벤트 기반 분석

### 5. Docker Compose
- 멀티 컨테이너 애플리케이션 관리
- 서비스 간 네트워킹
- 볼륨을 통한 데이터 영속성

### 6. 장애 대응
- 실시간 에러 모니터링
- 알림 규칙 설정
- 로그 기반 장애 분석

## 트러블슈팅

### Docker 컨테이너가 시작되지 않는 경우
```bash
# 로그 확인
docker-compose logs [service-name]

# 재시작
docker-compose restart
```

### Logstash 연결 실패
```bash
# Logstash 상태 확인
curl http://localhost:9600/_node/stats?pretty

# 포트 확인
lsof -i :5044
```

### Elasticsearch가 노란색(Yellow) 상태
- Single-node 클러스터이므로 정상입니다
- Replica가 없어서 노란색으로 표시됨

### 로그가 Elasticsearch에 저장되지 않는 경우
```bash
# Logstash 로그 확인
docker-compose logs logstash

# Spring Boot 로그 확인
# logs/ 디렉토리의 application.log 파일 확인

# Elasticsearch 인덱스 확인
curl http://localhost:9200/_cat/indices?v
```

## 성능 최적화

### 비동기 로깅
- AsyncAppender 사용으로 애플리케이션 성능 영향 최소화

### 로그 롤링
- 파일 크기: 10MB
- 보관 기간: 30일
- 전체 크기 제한: 1GB

### Elasticsearch 인덱싱
- 날짜별 인덱스 생성 (`application-logs-YYYY.MM.dd`)
- ILM (Index Lifecycle Management) 적용 가능

## 확장 가능성

### 운영 환경 적용 시 고려사항

1. **보안**
   - Elasticsearch 인증 활성화
   - SSL/TLS 적용
   - Kibana 사용자 인증

2. **고가용성**
   - Elasticsearch 클러스터 구성
   - Logstash 다중 인스턴스
   - Load Balancer 추가

3. **알림 통합**
   - Slack 연동
   - Email 알림
   - PagerDuty 통합

4. **성능 개선**
   - Elasticsearch 샤드 최적화
   - Logstash 파이프라인 튜닝
   - 캐싱 전략

## 참고 문서

- [Docker 가이드](./DOCKER_GUIDE.md)
- [로깅 가이드](./LOGGING_GUIDE.md)
- [API 예제](./API_EXAMPLES.md)
- [Kibana 가이드](./KIBANA_GUIDE.md)

## 라이선스

학습 목적의 프로젝트입니다.

## 기여

학습 프로젝트이므로 자유롭게 수정하여 사용하세요.
