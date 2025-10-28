# Week 3-4: Redis 캐시 성능 비교 테스트 가이드

## 목표
Week 1-2의 베이스라인 성능과 Redis 캐시 적용 후 성능을 비교하여 **캐시의 효과**를 측정합니다.

---

## 사전 준비

### 1. Redis 실행

```bash
cd /Users/r00416/Desktop/riman/study/high-traffic-mastery

# Redis 컨테이너 시작
docker-compose up -d redis

# Redis 상태 확인
docker-compose ps redis

# Redis 로그 확인
docker-compose logs redis
```

### 2. 애플리케이션 재빌드 및 실행

```bash
# 기존 애플리케이션 중지 (8080 포트 사용 중이면)
lsof -ti:8080 | xargs kill -9

# 재빌드 (Redis 의존성 추가됨)
./gradlew clean build

# 실행
./gradlew bootRun
```

### 3. API 테스트

```bash
# Health Check
curl http://localhost:8080/api/v3/products/health
# 응답: Week 3-4: Redis Cache OK

# 캐시 적용 API
curl http://localhost:8080/api/v3/products/cache/1

# 캐시 미적용 API
curl http://localhost:8080/api/v3/products/no-cache/1
```

---

## 테스트 시나리오

### 시나리오 1: 캐시 미적용 (Baseline)

**목적**: Week 1-2와 동일한 성능 측정 (비교 기준)

**nGrinder 설정**:
```
Test Name: Week3_Baseline_NoCache
Script: ProductWithCacheTest.groovy 수정 (no-cache 엔드포인트 사용)
Agent: 1
Vuser per agent: 100
Duration: 00:03:00
```

**스크립트 URL 수정**:
```groovy
// nGrinder 스크립트에서 다음 URL 사용
http://host.docker.internal:8080/api/v3/products/no-cache/{id}
```

**예상 결과**:
- TPS: 400-500
- 평균 응답시간: 150-200ms
- Peak TPS: 600

---

### 시나리오 2: 캐시 적용 (Redis)

**목적**: Redis 캐시 적용 후 성능 측정

**nGrinder 설정**:
```
Test Name: Week3_WithCache
Script: ProductWithCacheTest.groovy
Agent: 1
Vuser per agent: 100
Duration: 00:03:00
```

**스크립트 URL**:
```groovy
http://host.docker.internal:8080/api/v3/products/cache/{id}
```

**예상 결과**:
- TPS: 2000-3000 (4-6배 향상) ⬆️
- 평균 응답시간: 30-50ms (75% 단축) ⬇️
- Peak TPS: 3500+

---

### 시나리오 3: 동시 비교 테스트

**목적**: 캐시 적용/미적용 API를 동시에 테스트하여 직접 비교

**nGrinder 설정**:
```
Test Name: Week3_CacheComparison
Script: ProductCacheCompareTest.groovy
Agent: 1
Vuser per agent: 100
Duration: 00:03:00
```

**스크립트 특징**:
- `@Test` 메서드 2개
  - `testProductWithCache()` - 캐시 사용
  - `testProductWithoutCache()` - 캐시 미사용
- nGrinder가 자동으로 각 테스트의 TPS, 응답시간 분리 측정

**결과 분석**:
- Test 1 (WITH Cache) vs Test 2 (WITHOUT Cache)
- TPS 비율, 응답시간 비율 계산

---

## nGrinder 스크립트 작성

### 방법 1: 파일에서 복사

`/ngrinder-scripts/` 폴더에 준비된 스크립트를 nGrinder 웹 UI에 복사

### 방법 2: 직접 작성

**Script** 메뉴 → **Create** → 다음 코드 붙여넣기:

#### 캐시 적용 테스트

```groovy
import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse

@RunWith(GrinderRunner)
class ProductWithCacheTest {
    public static GTest test
    public static HTTPRequest request

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
        test = new GTest(1, "Product WITH Cache")
        request = new HTTPRequest()
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "testWithCache")
        grinder.statistics.delayReports = true
    }

    @Before
    public void before() {
        request.setHeaders([:])
    }

    @Test
    public void testWithCache() {
        def productId = (Math.random() * 100 + 1).toInteger()
        HTTPResponse response = request.GET(
            "http://host.docker.internal:8080/api/v3/products/cache/" + productId
        )
        assertThat(response.statusCode, is(200))
    }
}
```

---

## 성능 비교 표 작성

테스트 후 다음 표를 작성하여 비교하세요:

### 결과 기록 템플릿

| 지표 | Week 1-2 (캐시 없음) | Week 3-4 (Redis 캐시) | 개선율 |
|------|---------------------|----------------------|--------|
| **TPS** | ___ | ___ | ___% |
| **평균 응답시간** | ___ms | ___ms | ___% 단축 |
| **Peak TPS** | ___ | ___ | ___% |
| **95 Percentile** | ___ms | ___ms | ___% |
| **99 Percentile** | ___ms | ___ms | ___% |
| **Error Rate** | ___% | ___% | - |

### 예시

| 지표 | Week 1-2 | Week 3-4 | 개선율 |
|------|---------|---------|--------|
| **TPS** | 450 | 2500 | **456%** ⬆️ |
| **평균 응답시간** | 180ms | 35ms | **81%** 단축 ⬇️ |
| **Peak TPS** | 520 | 3200 | **515%** ⬆️ |
| **95 Percentile** | 250ms | 60ms | **76%** 단축 |
| **99 Percentile** | 400ms | 100ms | **75%** 단축 |
| **Error Rate** | 0% | 0% | - |

---

## 캐시 히트율 확인

### 애플리케이션 로그 확인

```bash
# 로그에서 캐시 Miss 확인
./gradlew bootRun | grep "CACHE MISS"

# 캐시 히트율 계산
# 캐시 히트 = 전체 요청 - CACHE MISS 로그 수
# 캐시 히트율 = (전체 요청 - CACHE MISS) / 전체 요청 × 100
```

### Redis 모니터링

```bash
# Redis CLI 접속
docker exec -it redis redis-cli

# 캐시된 키 확인
redis> KEYS product::*

# 특정 키 조회
redis> GET "product::1"

# 통계 확인
redis> INFO stats
```

---

## 문제 해결

### Redis 연결 실패

```bash
# Redis 재시작
docker-compose restart redis

# 애플리케이션 재시작
lsof -ti:8080 | xargs kill -9
./gradlew bootRun
```

### 캐시가 작동하지 않음

**확인 사항**:
1. `@EnableCaching` 어노테이션 확인
2. Redis가 실행 중인지 확인
3. 애플리케이션 로그에서 Redis 연결 에러 확인

```bash
# 애플리케이션 로그 확인
./gradlew bootRun 2>&1 | grep -i redis
```

### 성능이 기대보다 낮음

**원인**:
1. **Cold Start**: 첫 요청은 캐시 Miss → Warm-up 필요
2. **TTL 만료**: 5분마다 캐시 갱신 → 평균 성능 하락

**해결**:
```
Duration을 5분 이상으로 설정
Ramp-up 사용하여 점진적 부하 증가
```

---

## 분석 포인트

### 1. TPS 증가율
- **목표**: 3-5배 증가
- **원인**: DB 조회 → 메모리 조회로 변경

### 2. 응답시간 단축률
- **목표**: 70-80% 단축
- **원인**: DB I/O 제거, 네트워크 지연 감소

### 3. 캐시 히트율
- **목표**: 80% 이상
- **계산**: (전체 요청 - Cache Miss) / 전체 요청 × 100

### 4. CPU/메모리 사용량
- Redis 추가로 메모리 사용량 증가
- DB 부하 감소로 CPU 사용량 감소

---

## 다음 단계

성능 개선 확인 후:

1. **TTL 최적화**: 5분 → 10분 or 30분으로 조정
2. **Warm-up 전략**: 자주 조회되는 데이터 미리 캐싱
3. **Cache-Aside 패턴 이해**: 캐시 무효화 전략 학습
4. **Week 5-6 준비**: Kafka 비동기 처리 학습

---

## 참고 자료

- [Spring Cache 공식 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Redis 공식 문서](https://redis.io/documentation)
- [nGrinder 가이드](NGRINDER_GUIDE.md)

---

**Good luck with your Redis caching test!** 🚀
