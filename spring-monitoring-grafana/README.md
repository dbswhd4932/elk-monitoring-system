# Spring Boot Monitoring with Prometheus & Grafana

Spring Boot 애플리케이션의 메트릭을 프로메테우스로 수집하고 그라파나로 시각화하는 모니터링 시스템입니다.

## 프로젝트 구조

```
spring-monitoring-grafana/
├── src/
│   └── main/
│       ├── java/com/example/monitoring/
│       │   ├── MonitoringApplication.java
│       │   ├── config/
│       │   │   └── MetricsConfig.java
│       │   ├── controller/
│       │   │   └── DemoController.java
│       │   └── service/
│       │       └── DemoService.java
│       └── resources/
│           └── application.yml
├── prometheus/
│   └── prometheus.yml
├── grafana/
│   ├── dashboards/
│   │   └── spring-boot-dashboard.json
│   └── provisioning/
│       ├── datasources/
│       │   └── datasource.yml
│       └── dashboards/
│           └── dashboard.yml
├── docker-compose.yml
├── Dockerfile
└── build.gradle
```

## 기술 스택

- **Spring Boot 3.2.0**: 애플리케이션 프레임워크
- **Spring Boot Actuator**: 메트릭 엔드포인트 제공
- **Micrometer**: 메트릭 수집 및 관리
- **Prometheus**: 메트릭 저장 및 쿼리
- **Grafana**: 메트릭 시각화 대시보드
- **Docker & Docker Compose**: 컨테이너화

## 주요 기능

### 1. 메트릭 수집
- HTTP 요청 메트릭 (요청 수, 응답 시간, 상태 코드)
- JVM 메트릭 (메모리, GC, 쓰레드)
- 시스템 메트릭 (CPU, 프로세서)
- 커스텀 메트릭 (Counter, Timer)

### 2. 모니터링 대시보드
- HTTP 요청 레이트
- HTTP 요청 응답 시간
- JVM 힙 메모리 사용량
- 시스템 CPU 사용량
- JVM 쓰레드 수

### 3. API 엔드포인트
- `GET /api/hello?name={name}`: 인사 메시지 반환
- `GET /api/fast`: 빠른 응답 테스트
- `GET /api/slow`: 느린 응답 테스트 (500-1500ms)
- `POST /api/data`: 데이터 처리
- `GET /api/error`: 에러 발생 테스트

## 시작하기

### 사전 요구사항
- Docker & Docker Compose
- Java 17 (로컬 실행 시)
- Gradle (로컬 실행 시)

### Docker Compose로 실행

1. 프로젝트 클론 및 이동
```bash
cd spring-monitoring-grafana
```

2. Docker Compose로 전체 스택 실행
```bash
docker-compose up -d
```

3. 서비스 접속
- **Spring Boot App**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (ID: admin, PW: admin)

### 로컬에서 실행

1. Prometheus와 Grafana만 Docker로 실행
```bash
docker-compose up -d prometheus grafana
```

2. Spring Boot 애플리케이션 실행
```bash
./gradlew bootRun
```

## 사용 방법

### 1. 메트릭 확인

Spring Boot Actuator 엔드포인트:
```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# 메트릭 목록
curl http://localhost:8080/actuator/metrics

# 프로메테우스 형식 메트릭
curl http://localhost:8080/actuator/prometheus
```

### 2. API 테스트

```bash
# 기본 요청
curl http://localhost:8080/api/hello

# 이름 지정
curl http://localhost:8080/api/hello?name=John

# 빠른 응답
curl http://localhost:8080/api/fast

# 느린 응답
curl http://localhost:8080/api/slow

# 데이터 전송
curl -X POST http://localhost:8080/api/data \
  -H "Content-Type: application/json" \
  -d "test data"

# 에러 발생
curl http://localhost:8080/api/error
```

### 3. 부하 테스트 (선택사항)

```bash
# Apache Bench 사용 예시
ab -n 1000 -c 10 http://localhost:8080/api/hello

# 또는 반복 curl
for i in {1..100}; do curl http://localhost:8080/api/fast; done
```

### 4. Prometheus 확인

http://localhost:9090 접속 후 쿼리 예시:
```promql
# HTTP 요청 레이트
rate(http_server_requests_seconds_count[1m])

# 평균 응답 시간
rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])

# JVM 메모리 사용량
jvm_memory_used_bytes{area="heap"}

# CPU 사용률
system_cpu_usage
```

### 5. Grafana 대시보드

1. http://localhost:3000 접속
2. 로그인 (admin/admin)
3. "Spring Boot Application Monitoring" 대시보드 자동 로드됨
4. 시간 범위 조정 (우측 상단)

## 커스텀 메트릭 추가

### Counter 예시
```java
private final Counter customCounter;

public MyService(MeterRegistry meterRegistry) {
    this.customCounter = Counter.builder("custom.counter")
        .description("Custom counter description")
        .tag("type", "example")
        .register(meterRegistry);
}

public void doSomething() {
    customCounter.increment();
    // 비즈니스 로직
}
```

### Timer 예시
```java
@Timed(value = "custom.timer", description = "Custom timer description")
public String timedMethod() {
    // 메서드 실행 시간 자동 측정
    return "result";
}
```

## 모니터링 메트릭 설명

### HTTP 메트릭
- `http_server_requests_seconds_count`: HTTP 요청 수
- `http_server_requests_seconds_sum`: HTTP 요청 총 소요 시간
- `http_server_requests_seconds_max`: HTTP 요청 최대 소요 시간

### JVM 메트릭
- `jvm_memory_used_bytes`: JVM 메모리 사용량
- `jvm_memory_max_bytes`: JVM 메모리 최대값
- `jvm_threads_live_threads`: 라이브 쓰레드 수
- `jvm_gc_pause_seconds`: GC 일시정지 시간

### 시스템 메트릭
- `system_cpu_usage`: 시스템 CPU 사용률
- `process_cpu_usage`: 프로세스 CPU 사용률

## 문제 해결

### Docker Compose 오류
```bash
# 컨테이너 로그 확인
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f spring-app
docker-compose logs -f prometheus
docker-compose logs -f grafana

# 컨테이너 재시작
docker-compose restart

# 완전히 재시작
docker-compose down
docker-compose up -d
```

### Prometheus가 메트릭을 수집하지 못하는 경우
1. Spring Boot 애플리케이션이 실행 중인지 확인
2. http://localhost:8080/actuator/prometheus 접속 확인
3. prometheus.yml의 타겟 설정 확인
4. Docker 네트워크 확인

### Grafana 대시보드가 보이지 않는 경우
1. Prometheus 데이터소스 연결 확인 (Configuration > Data Sources)
2. 대시보드 프로비저닝 디렉토리 확인
3. 볼륨 마운트 확인

## 참고 자료

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
- [Grafana](https://grafana.com/docs/)

## 라이센스

MIT License
