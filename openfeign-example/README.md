# OpenFeign 학습 예제 프로젝트

Spring Cloud OpenFeign을 사용한 마이크로서비스 간 통신 학습 프로젝트입니다.

## 프로젝트 구조

이 프로젝트는 두 개의 서비스로 구성되어 있습니다:

1. **User Service** (포트 8080)
   - 사용자 정보를 제공하는 API 서버
   - REST API를 통해 사용자 CRUD 작업 제공

2. **Order Service** (포트 8081)
   - 주문 정보를 관리하는 API 서버
   - OpenFeign을 사용하여 User Service와 통신
   - 주문 조회 시 자동으로 사용자 정보를 가져옴

## 기술 스택

- Spring Boot 3.2.0
- Spring Cloud OpenFeign
- Lombok
- Java 17

## OpenFeign 주요 기능

### 1. @FeignClient 어노테이션
```java
@FeignClient(
    name = "user-service",
    url = "${user.service.url}",
    configuration = FeignConfig.class
)
public interface UserClient {
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}
```

### 2. 설정 옵션
- **타임아웃 설정**: 연결 및 읽기 타임아웃
- **재시도 정책**: 실패 시 재시도 로직
- **로깅 레벨**: 요청/응답 로깅 상세도

### 3. 로깅 레벨
- `NONE`: 로깅 안함
- `BASIC`: 요청 메서드, URL, 응답 상태, 실행 시간
- `HEADERS`: BASIC + 헤더 정보
- `FULL`: 모든 요청/응답 데이터

## 실행 방법

### 1. User Service 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=user-service'
```

### 2. Order Service 실행 (새 터미널에서)
```bash
./gradlew bootRun --args='--spring.profiles.active=order-service'
```

## API 테스트

### User Service API (포트 8080)

#### 1. 모든 사용자 조회
```bash
curl http://localhost:8080/api/users
```

#### 2. 특정 사용자 조회
```bash
curl http://localhost:8080/api/users/1
```

#### 3. 사용자 생성
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4,
    "name": "최지훈",
    "email": "choi@example.com",
    "phone": "010-4567-8901"
  }'
```

### Order Service API (포트 8081)

#### 1. 모든 주문 조회 (Feign 사용)
```bash
curl http://localhost:8081/api/orders
```
이 요청은 내부적으로 User Service를 호출하여 각 주문의 사용자 정보를 가져옵니다.

#### 2. 특정 주문 조회 (Feign 사용)
```bash
curl http://localhost:8081/api/orders/1
```

#### 3. 특정 사용자의 주문 조회 (Feign 사용)
```bash
curl http://localhost:8081/api/orders/user/1
```

#### 4. 새 주문 생성 (Feign으로 사용자 검증)
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4,
    "userId": 2,
    "productName": "모니터",
    "quantity": 1,
    "price": 350000.0
  }'
```

## 학습 포인트

### 1. Feign Client 인터페이스 정의
`UserClient.java` 파일을 확인하여 Feign Client를 어떻게 정의하는지 학습하세요.

### 2. Feign 설정
`FeignConfig.java` 파일에서 타임아웃, 재시도, 로깅 등을 설정하는 방법을 확인하세요.

### 3. Feign 사용
`OrderService.java` 파일에서 Feign Client를 주입받아 사용하는 방법을 확인하세요.

### 4. 로깅 확인
Order Service를 실행하고 API를 호출하면 콘솔에서 Feign의 상세한 로그를 확인할 수 있습니다:
- 요청 URL
- 요청 헤더
- 요청 바디
- 응답 상태
- 응답 헤더
- 응답 바디
- 실행 시간

## 에러 처리 테스트

### 1. 존재하지 않는 사용자로 주문 생성 시도
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "id": 5,
    "userId": 999,
    "productName": "테스트 상품",
    "quantity": 1,
    "price": 10000.0
  }'
```
Feign이 404 에러를 어떻게 처리하는지 확인할 수 있습니다.

### 2. User Service 중단 후 Order Service 호출
User Service를 중단한 후 Order Service API를 호출하면 타임아웃과 재시도 로직을 확인할 수 있습니다.

## 고급 학습 주제

### 1. Feign Fallback
서비스가 실패했을 때 대체 로직을 실행하도록 설정할 수 있습니다.

### 2. Feign Interceptor
모든 요청에 공통 헤더(예: 인증 토큰)를 추가할 수 있습니다.

### 3. Circuit Breaker
Resilience4j와 통합하여 서킷 브레이커 패턴을 적용할 수 있습니다.

### 4. Service Discovery
Eureka 또는 Consul과 통합하여 동적 서비스 디스커버리를 구현할 수 있습니다.

## 참고 자료

- [Spring Cloud OpenFeign 공식 문서](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
- [Feign GitHub](https://github.com/OpenFeign/feign)

## 프로젝트 구조
```
openfeign-example/
├── build.gradle
├── settings.gradle
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── openfeign/
        │               ├── UserServiceApplication.java      # User Service 메인
        │               ├── OrderServiceApplication.java     # Order Service 메인
        │               ├── common/
        │               │   └── User.java                    # 공통 DTO
        │               ├── user/
        │               │   └── UserController.java          # User API
        │               └── order/
        │                   ├── Order.java                   # Order DTO
        │                   ├── UserClient.java              # Feign Client 인터페이스
        │                   ├── FeignConfig.java             # Feign 설정
        │                   ├── OrderService.java            # Order 비즈니스 로직
        │                   └── OrderController.java         # Order API
        └── resources/
            ├── application.yml                              # 설정 파일
            └── logback-spring.xml                           # 로깅 설정
```
