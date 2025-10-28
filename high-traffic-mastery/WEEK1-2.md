# Week 1-2: 기본 성능 측정

## 📖 필독 가이드

**nGrinder를 처음 사용한다면 먼저 읽어보세요:**
- **[NGRINDER_GUIDE.md](NGRINDER_GUIDE.md)** - nGrinder 완벽 가이드 (Agent 설정부터 결과 분석까지)

## 학습 목표
1. nGrinder 사용법 익히기
2. 성능 지표 이해 (TPS, Latency, Error Rate)
3. 병목 지점 파악하기
4. 베이스라인 성능 측정

## 실습 순서

### 1단계: 애플리케이션 실행 및 확인

```bash
# 1. 빌드
./gradlew clean build

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. Health Check
curl http://localhost:8080/api/v1/products/health
# 응답: OK

# 4. 상품 조회 테스트
curl http://localhost:8080/api/v1/products/1
# 응답: {"id":1,"name":"상품 1","price":1000,"description":"상품 설명 1","stock":1000}

# 5. 전체 상품 조회
curl http://localhost:8080/api/v1/products
```

**기대 결과**:
- 100개 상품이 초기 데이터로 생성됨
- 각 API가 정상 응답

---

### 2단계: nGrinder 환경 구성

```bash
# 1. nGrinder 컨테이너 실행
docker-compose up -d ngrinder-controller ngrinder-agent

# 2. 로그 확인 (컨트롤러가 완전히 시작될 때까지 1-2분 소요)
docker-compose logs -f ngrinder-controller

# 3. nGrinder 접속
# 브라우저에서 http://localhost:8300 열기
# 기본 계정: admin / admin
```

**트러블슈팅**:
- 포트 충돌 시: `docker-compose down` 후 재시작
- Agent가 연결 안되면: 몇 분 기다린 후 새로고침

---

### 3단계: nGrinder 스크립트 작성

**nGrinder 웹 UI에서**:

1. **Script** 메뉴 클릭
2. **Create** 버튼 클릭
3. 아래 내용 입력:
   - Script Name: `ProductLoadTest`
   - Script Type: `Groovy`

**스크립트 내용**:

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
class ProductLoadTest {

    public static GTest test
    public static HTTPRequest request
    public static Map<String, String> headers = [:]

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
        test = new GTest(1, "Product API - GET /products/{id}")
        request = new HTTPRequest()
        grinder.logger.info("before process.")
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "testGetProduct")
        grinder.statistics.delayReports = true
        grinder.logger.info("before thread.")
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        grinder.logger.info("before. init headers")
    }

    @Test
    public void testGetProduct() {
        // 1~100 사이 랜덤 상품 ID 조회
        def productId = (Math.random() * 100 + 1).toInteger()
        HTTPResponse response = request.GET("http://host.docker.internal:8080/api/v1/products/" + productId)

        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }
    }
}
```

4. **Validate** 버튼으로 문법 검증
5. **Save** 버튼으로 저장

---

### 4단계: 성능 테스트 실행

**nGrinder 웹 UI에서**:

1. **Performance Test** 메뉴 클릭
2. **Create Test** 버튼 클릭
3. 설정값 입력:

```
Test Name: Week1_Baseline_100VUser
Agent: 1
Vuser per agent: 100
Script: ProductLoadTest
Duration: 00:02:00 (2분)
Run Count: 무한 (비워두기)
Ramp-Up:
  - Enable Ramp-Up: Yes
  - Initial Count: 10
  - Interval: 1000ms
```

4. **Save and Start** 버튼 클릭

---

### 5단계: 결과 분석

테스트가 완료되면 다음 지표를 확인:

**기본 지표**:
- **TPS** (Transactions Per Second): 초당 처리량
- **Mean Test Time (MTT)**: 평균 응답 시간
- **Peak TPS**: 최대 TPS
- **Errors**: 에러 발생 수

**목표 지표**:
| 지표 | 목표 | 비고 |
|------|------|------|
| TPS | 500+ | 초당 500개 요청 처리 |
| 평균 응답시간 | 200ms 이하 | |
| Peak TPS | 600+ | |
| Error Rate | 0% | 에러 없어야 함 |

**예상 결과** (simulateProcessing 50ms 기준):
- TPS: ~200 (50ms 처리시간 + DB 조회)
- 평균 응답시간: ~100-150ms

---

### 6단계: 테스트 시나리오 확대

#### Test 2: 동시 사용자 500명

```
Test Name: Week1_Baseline_500VUser
Vuser per agent: 500
Duration: 00:03:00
```

**예상**:
- TPS 증가
- 평균 응답시간 증가
- 병목 현상 발생 가능

#### Test 3: 동시 사용자 1000명

```
Test Name: Week1_Baseline_1000VUser
Vuser per agent: 1000
Duration: 00:05:00
```

**예상**:
- Thread Pool 고갈 가능
- 응답시간 급격히 증가
- 에러 발생 가능

---

### 7단계: 병목 지점 분석

#### 애플리케이션 로그 확인

```bash
# 실시간 로그 모니터링
./gradlew bootRun

# Thread Pool 상태 확인
curl http://localhost:8080/actuator/metrics/tomcat.threads.busy
curl http://localhost:8080/actuator/metrics/tomcat.threads.config.max
```

#### JVM 메트릭 확인

```bash
# Heap 메모리 사용량
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# GC 횟수
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
```

---

### 8단계: 성능 개선 실험

#### 실험 1: Thread Pool 크기 조정

`application.yml` 수정:

```yaml
server:
  tomcat:
    threads:
      max: 400  # 200 -> 400
      min-spare: 20  # 10 -> 20
```

재시작 후 동일한 테스트 실행하여 비교

#### 실험 2: 처리 시간 단축

`ProductService.java` 의 `simulateProcessing(50)` 값을 변경:
- 50ms -> 10ms로 줄이기
- 성능이 얼마나 개선되는지 측정

---

## 학습 체크리스트

- [ ] Spring Boot 애플리케이션이 정상 실행됨
- [ ] nGrinder 접속 및 로그인 성공
- [ ] 첫 번째 부하 테스트 스크립트 작성 완료
- [ ] 100 VUser 테스트 실행 및 결과 확인
- [ ] 500 VUser 테스트로 확대 실행
- [ ] 1000 VUser 테스트로 병목 지점 확인
- [ ] TPS, 평균 응답시간 지표 이해
- [ ] Thread Pool 설정 변경 후 성능 비교
- [ ] 처리 시간 변경 후 성능 비교

---

## 결과 기록 템플릿

테스트할 때마다 아래 형식으로 기록하세요:

```
### Test 1: 100 VUser (Baseline)
- 테스트 일시: 2024-01-XX
- TPS: XXX
- 평균 응답시간: XXms
- Peak TPS: XXX
- Error Rate: X%
- 병목: Thread Pool / DB / Network / 기타

### Test 2: 500 VUser
- TPS: XXX
- 평균 응답시간: XXms
- ...

### Test 3: Thread Pool 조정 후
- 설정: max=400, min-spare=20
- TPS: XXX (Before: XXX, +XX% 향상)
- 평균 응답시간: XXms (Before: XXms)
```

---

## 다음 단계 (Week 3-4)

Week 1-2에서 측정한 베이스라인 성능을 기록해두세요.
Week 3-4에서는 **Redis 캐싱**을 적용하여 성능을 개선합니다.

**예상 개선**:
- TPS: 500 -> 2000+ (4배 향상)
- 응답시간: 200ms -> 50ms (75% 단축)

---

## 참고 자료

- [nGrinder 가이드](http://naver.github.io/ngrinder/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Tomcat Tuning](https://tomcat.apache.org/tomcat-9.0-doc/config/http.html)
