# High Traffic Mastery Project

대용량 트래픽 처리를 위한 단계별 학습 프로젝트

## 프로젝트 목표

nGrinder를 활용한 부하 테스트를 통해 대용량 트래픽 처리 기술을 학습하고 실습합니다.

## 📖 학습 가이드

- **[START_HERE.md](START_HERE.md)** - 프로젝트 시작 가이드
- **[NGRINDER_GUIDE.md](NGRINDER_GUIDE.md)** - ⭐ **nGrinder 완벽 가이드 (처음부터 끝까지)**
- **[WEEK1-2.md](WEEK1-2.md)** - Week 1-2 상세 학습 가이드

## 기술 스택

- **Backend**: Spring Boot 3.2, JPA
- **Cache**: Redis, Redisson
- **Message Queue**: Kafka
- **Database**: H2 (초기), PostgreSQL (Week 7+)
- **Load Testing**: nGrinder
- **Monitoring**: Prometheus, Grafana

## 주차별 학습 계획

### Week 1-2: 기본 성능 측정
- [x] Spring Boot API 서버 구축
- [ ] nGrinder 환경 구성
- [ ] 베이스라인 성능 측정 (TPS, Latency)

**학습 목표**:
- nGrinder 사용법 익히기
- 성능 지표 이해 (TPS, 응답시간, 에러율)
- 병목 지점 파악

**실습 내용**:
```bash
# 1. 애플리케이션 실행
./gradlew bootRun

# 2. nGrinder 실행
docker-compose up -d ngrinder-controller ngrinder-agent

# 3. nGrinder 접속
http://localhost:8300
기본 계정: admin / admin

# 4. 테스트 대상 API
GET http://localhost:8080/api/v1/products/{id}
```

**측정 지표**:
- 동시 사용자: 100명
- TPS 목표: 500+
- 평균 응답시간: 200ms 이하

---

### Week 3-4: Redis 캐싱 적용
- [ ] Look-Aside 캐싱 전략 구현
- [ ] 캐시 히트율 측정
- [ ] 성능 개선 비교 (Before/After)

**학습 목표**:
- Redis 캐싱 전략 이해
- 캐시 히트율 최적화
- TTL 설정 전략

**실습 내용**:
```bash
# Redis 실행
docker-compose up -d redis

# 캐시 적용 후 성능 비교
- TPS 목표: 2000+
- 평균 응답시간: 50ms 이하
- 캐시 히트율: 80%+
```

**구현 파일**: `week3/`

---

### Week 5-6: Kafka 비동기 처리
- [ ] Producer/Consumer 구현
- [ ] 주문-결제-알림 파이프라인
- [ ] 처리량(Throughput) 측정

**학습 목표**:
- Kafka를 통한 비동기 처리
- 이벤트 기반 아키텍처
- 컨슈머 그룹 스케일링

**실습 내용**:
```bash
# Kafka 실행
docker-compose up -d zookeeper kafka

# 성능 목표
- 초당 메시지 처리: 10,000+
- End-to-End Latency: 500ms 이하
```

**구현 파일**: `week5/`

---

### Week 7-8: DB 최적화
- [ ] PostgreSQL 전환
- [ ] 쿼리 최적화 (Explain Plan)
- [ ] Connection Pool 튜닝
- [ ] Read Replica 구성

**학습 목표**:
- 인덱스 전략
- N+1 문제 해결
- Master-Slave 읽기/쓰기 분리

**실습 내용**:
```bash
# PostgreSQL 실행
docker-compose up -d postgres-master

# 성능 목표
- 100만 건 데이터 조회: 100ms 이하
- Connection Pool 최적화
```

**구현 파일**: `week7/`

---

### Week 9-11: 통합 프로젝트
- [ ] 티켓 예매 시스템 (동시성 제어)
- [ ] 플래시 세일 시스템
- [ ] 종합 성능 테스트

**학습 목표**:
- Redis 분산 락
- 재고 관리 (동시성 제어)
- 전체 기술 통합

**성능 목표**:
- TPS: 10,000+
- 평균 응답시간: 100ms 이하
- 99 percentile: 500ms 이하

**구현 파일**: `week9/`

---

### Week 12: 모니터링 & 튜닝
- [ ] Prometheus + Grafana 대시보드
- [ ] JVM 프로파일링
- [ ] 최종 성능 튜닝

**학습 목표**:
- 실시간 모니터링 구축
- 병목 지점 분석 및 개선
- 최종 성능 검증

**실습 내용**:
```bash
# 모니터링 스택 실행
docker-compose up -d prometheus grafana

# Grafana 접속
http://localhost:3000
기본 계정: admin / admin
```

---

## 시작하기

### 1. 프로젝트 클론 및 빌드

```bash
cd high-traffic-mastery
./gradlew clean build
```

### 2. Docker 환경 실행

```bash
# Week 1-2: nGrinder만 실행
docker-compose up -d ngrinder-controller ngrinder-agent

# Week 3-4: Redis 추가
docker-compose up -d redis

# Week 5-6: Kafka 추가
docker-compose up -d zookeeper kafka

# Week 7-8: PostgreSQL 추가
docker-compose up -d postgres-master

# Week 12: 모니터링 추가
docker-compose up -d prometheus grafana
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. Health Check

```bash
curl http://localhost:8080/api/v1/products/health
# 응답: OK

curl http://localhost:8080/actuator/health
# 응답: {"status":"UP"}
```

---

## nGrinder 사용 가이드

### 1. 접속 및 로그인
- URL: http://localhost:8300
- 계정: admin / admin

### 2. 스크립트 생성
1. Script 메뉴 클릭
2. "Create" 버튼 클릭
3. Script 작성 (Groovy)

**샘플 스크립트**:
```groovy
import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse
import org.ngrinder.http.cookie.Cookie
import org.ngrinder.http.cookie.CookieManager

@RunWith(GrinderRunner)
class TestRunner {

    public static GTest test
    public static HTTPRequest request
    public static Map<String, String> headers = [:]
    public static List<Cookie> cookies = []

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
        test = new GTest(1, "Product API Test")
        request = new HTTPRequest()
        grinder.logger.info("before process.")
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
        grinder.statistics.delayReports = true
        grinder.logger.info("before thread.")
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        CookieManager.addCookies(cookies)
        grinder.logger.info("before. init headers and cookies")
    }

    @Test
    public void test() {
        // 1~100 사이 랜덤 상품 ID 조회
        def productId = (Math.random() * 100 + 1).toInteger()
        HTTPResponse response = request.GET("http://host.docker.internal:8080/api/v1/products/" + productId, [:])

        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
    }
}
```

### 3. 테스트 설정
1. Performance Test 메뉴 클릭
2. "Create Test" 버튼 클릭
3. 설정값 입력:
   - Agent: 1
   - Vuser per agent: 100 (동시 사용자)
   - Duration: 2분
   - Script: 위에서 생성한 스크립트 선택

### 4. 테스트 실행 및 결과 분석
- "Start" 버튼 클릭
- 실시간 그래프 확인
- TPS, 평균 응답시간, 에러율 확인

---

## 성능 측정 기준

### 주요 지표
- **TPS** (Transactions Per Second): 초당 처리량
- **Mean Response Time**: 평균 응답 시간
- **95/99 Percentile**: 상위 5%, 1% 응답 시간
- **Error Rate**: 에러 발생률

### 단계별 목표

| Week | TPS | 평균 응답시간 | 99 Percentile |
|------|-----|--------------|---------------|
| 1-2  | 500+ | 200ms | 500ms |
| 3-4  | 2000+ | 50ms | 200ms |
| 5-6  | 5000+ | 100ms | 300ms |
| 7-8  | 8000+ | 80ms | 250ms |
| 9-11 | 10000+ | 100ms | 500ms |

---

## 트러블슈팅

### nGrinder가 host.docker.internal에 접근 못할 때
```bash
# Docker Desktop 설정에서 "Enable host networking" 확인
# 또는 docker-compose.yml에서 extra_hosts 추가
```

### Redis 연결 안될 때
```bash
docker-compose logs redis
# Redis가 정상 실행 중인지 확인
```

### Kafka 연결 안될 때
```bash
docker-compose logs kafka
# Zookeeper와 Kafka가 모두 실행 중인지 확인
```

---

## 학습 리소스

- [nGrinder 공식 문서](http://naver.github.io/ngrinder/)
- [Spring Boot Performance Tuning](https://spring.io/guides)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)
- [Kafka Tutorials](https://kafka.apache.org/documentation/)

---

## 참고사항

- 각 주차별로 별도의 패키지로 구성 (`week1`, `week3`, `week5`, ...)
- 점진적으로 기능을 추가하면서 성능을 측정하고 개선
- 매 주차 성능 측정 결과를 기록하고 비교

**Good luck!** 🚀
