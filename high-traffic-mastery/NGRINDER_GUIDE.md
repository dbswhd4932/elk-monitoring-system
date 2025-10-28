# nGrinder 완벽 가이드 - 처음부터 끝까지

nGrinder를 처음 사용하는 분들을 위한 완벽한 단계별 가이드입니다.

---

## 목차
1. [nGrinder 시작하기](#1-ngrinder-시작하기)
2. [Agent 설정 및 관리](#2-agent-설정-및-관리)
3. [스크립트 작성](#3-스크립트-작성)
4. [Performance Test 생성 및 실행](#4-performance-test-생성-및-실행)
5. [결과 분석](#5-결과-분석)
6. [문제 해결](#6-문제-해결)

---

## 1. nGrinder 시작하기

### 1.1 Docker 컨테이너 실행

```bash
# 프로젝트 디렉토리로 이동
cd /Users/r00416/Desktop/riman/study/high-traffic-mastery

# nGrinder Controller와 Agent 실행
docker-compose up -d ngrinder-controller ngrinder-agent
```

**출력 예시:**
```
✔ Container ngrinder-controller  Started
✔ Container ngrinder-agent       Started
```

### 1.2 컨테이너 상태 확인

```bash
# 컨테이너가 정상 실행 중인지 확인
docker-compose ps

# 출력 예시:
# NAME                  STATUS          PORTS
# ngrinder-controller   Up 1 minute     0.0.0.0:8300->80/tcp
# ngrinder-agent        Up 1 minute
```

### 1.3 로그 확인 (Controller가 완전히 시작될 때까지 1-2분 소요)

```bash
# Controller 로그 실시간 확인
docker-compose logs -f ngrinder-controller

# 다음 메시지가 나올 때까지 대기:
# "Tomcat started on port(s): 80 (http)"
# "Started NGrinderControllerStarter"

# Ctrl + C 로 로그 종료
```

### 1.4 웹 브라우저에서 접속

1. **브라우저 열기**: Chrome, Safari 등
2. **URL 입력**: http://localhost:8300
3. **로그인 정보 입력**:
   - **User ID**: `admin`
   - **Password**: `admin`

**첫 로그인 시 비밀번호 변경 요구가 나올 수 있습니다** (선택사항)

---

## 2. Agent 설정 및 관리

### 2.1 Agent Management 페이지 이동

1. **로그인 후 상단 메뉴에서 "Admin" 클릭**
2. **왼쪽 메뉴에서 "Agent Management" 클릭**

### 2.2 Agent 상태 확인

Agent Management 페이지에서 다음을 확인:

| 항목 | 설명 |
|------|------|
| **Agent Name** | `ngrinder-agent_ngrinder-agent_1` 형식 |
| **IP** | Docker 내부 IP |
| **Status** | **Approved** (승인됨) 또는 **Ready** |
| **Approved** | ✓ 체크되어 있어야 함 |

### 2.3 Agent가 "Unapproved" 상태일 때

만약 Agent 상태가 "Unapproved"라면:

1. **Agent 옆의 체크박스 선택**
2. **"Approve" 버튼 클릭**
3. **페이지 새로고침** (F5)
4. **Status가 "Ready"로 변경되었는지 확인**

### 2.4 Agent 연결 문제 해결

Agent가 목록에 보이지 않으면:

```bash
# Agent 재시작
docker-compose restart ngrinder-agent

# 1분 대기
sleep 60

# 다시 웹 페이지 새로고침
```

여전히 안 보이면:

```bash
# 전체 재시작
docker-compose down
docker-compose up -d ngrinder-controller ngrinder-agent

# 2-3분 대기
sleep 180
```

---

## 3. 스크립트 작성

### 3.1 Script 메뉴로 이동

1. **상단 메뉴에서 "Script" 클릭**
2. **오른쪽 상단 "Create" 버튼 클릭**

### 3.2 스크립트 생성

**Create a Script 팝업에서:**

| 필드 | 값 |
|------|-----|
| **Script Name** | `ProductLoadTest` |
| **Create a new script** | 선택 |
| **Script Type** | **Groovy** 선택 |

**"Create" 버튼 클릭**

### 3.3 스크립트 코드 입력

에디터에 다음 코드를 붙여넣기:

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

/**
 * Week 1-2: Product API Load Test
 * 상품 조회 API의 성능을 측정합니다.
 */
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
        grinder.logger.info("=== Test Process Started ===")
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "testGetProduct")
        grinder.statistics.delayReports = true
        grinder.logger.info("=== Test Thread Started ===")
    }

    @Before
    public void before() {
        request.setHeaders(headers)
    }

    @Test
    public void testGetProduct() {
        // 1~100 사이 랜덤 상품 ID 조회
        def productId = (Math.random() * 100 + 1).toInteger()

        // API 호출 - Docker 컨테이너에서 호스트 머신의 8080 포트로 접근
        HTTPResponse response = request.GET(
            "http://host.docker.internal:8080/api/v1/products/" + productId
        )

        // 응답 코드 검증
        if (response.statusCode == 301 || response.statusCode == 302) {
            grinder.logger.warn("Warning: Redirect response code {}.", response.statusCode)
        } else {
            assertThat(response.statusCode, is(200))
        }

        // 디버깅: 응답 본문 로그 (선택사항)
        // grinder.logger.info("Response: {}", response.getText())
    }
}
```

### 3.4 스크립트 검증 및 저장

1. **"Validate" 버튼 클릭** - 문법 오류 확인
2. **오류가 없으면 "Save" 버튼 클릭**
3. **Script 목록으로 돌아감**

---

## 4. Performance Test 생성 및 실행

### 4.1 Performance Test 메뉴로 이동

1. **상단 메뉴에서 "Performance Test" 클릭**
2. **오른쪽 상단 "Create Test" 버튼 클릭**

### 4.2 테스트 설정

#### 기본 정보 (Basic Configuration)

| 필드 | 값 | 설명 |
|------|-----|------|
| **Test Name** | `Week1_Baseline_100VUser` | 테스트 이름 |
| **Tag** | (비워둠) | 선택사항 |
| **Description** | `Week 1-2 베이스라인 성능 측정` | 설명 |

#### Agent 설정

| 필드 | 값 | 설명 |
|------|-----|------|
| **Agent** | `1` | **사용할 Agent 머신 수** |
| **Vuser per agent** | `100` | **각 Agent당 가상 사용자 수** |

> **총 가상 사용자 = Agent × Vuser per agent = 1 × 100 = 100명**

#### Script 설정

| 필드 | 값 |
|------|-----|
| **Script** | `ProductLoadTest` (위에서 작성한 스크립트 선택) |

#### Duration 설정

| 필드 | 값 | 설명 |
|------|-----|------|
| **Duration** | `00:02:00` | 2분 동안 실행 (시:분:초) |
| **Run Count** | 비워둠 | 무한 반복 (Duration 동안) |

#### Ramp-Up 설정 (선택사항)

| 필드 | 값 | 설명 |
|------|-----|------|
| **Enable Ramp-Up** | ✓ 체크 | 점진적으로 부하 증가 |
| **Initial Count** | `10` | 시작 시 가상 사용자 10명 |
| **Initial Sleep Time** | `0` | 대기 시간 없음 |
| **Incremental Step** | `10` | 매번 10명씩 증가 |
| **Interval** | `1000` | 1초마다 증가 |

### 4.3 테스트 실행

1. **"Save and Start" 버튼 클릭**
2. **테스트 시작 확인 팝업에서 "Start" 클릭**

### 4.4 실시간 모니터링

테스트 실행 중 화면:

| 표시 항목 | 설명 |
|----------|------|
| **TPS** | Transactions Per Second (초당 처리량) |
| **Mean Test Time (MTT)** | 평균 응답 시간 (ms) |
| **Executed Tests** | 총 실행된 테스트 수 |
| **Successful Tests** | 성공한 테스트 수 |
| **Errors** | 에러 발생 수 |
| **Vusers** | 현재 활성 가상 사용자 수 |

**실시간 그래프**:
- **TPS 그래프**: 초당 처리량 추이
- **Mean Test Time 그래프**: 평균 응답 시간 추이
- **Vuser 그래프**: 활성 사용자 수 추이

### 4.5 테스트 중지

필요시 **"Stop" 버튼** 클릭하여 중간에 중지 가능

---

## 5. 결과 분석

### 5.1 테스트 완료 후

테스트가 완료되면 자동으로 **"Detail Report"** 페이지로 이동합니다.

### 5.2 주요 지표 해석

#### Summary 섹션

| 지표 | 설명 | Week 1-2 목표 |
|------|------|---------------|
| **TPS** | 초당 처리량 | **500+** |
| **Peak TPS** | 최대 TPS | 600+ |
| **Mean Test Time (MTT)** | 평균 응답 시간 | **200ms 이하** |
| **Executed Tests** | 총 테스트 실행 수 | - |
| **Successful Tests** | 성공한 테스트 수 | - |
| **Errors** | 에러 수 | **0** |
| **Run time** | 총 실행 시간 | 2분 |

#### Detailed Report

**TPS Graph**:
- 시간에 따른 TPS 변화
- Ramp-up 구간에서 점진적 증가 확인

**Response Time Graph**:
- 평균, 최소, 최대 응답 시간
- 95 Percentile, 99 Percentile 확인

**Vuser Graph**:
- 활성 가상 사용자 수 변화

### 5.3 결과 다운로드

**"Download CSV" 버튼** 클릭하여 상세 결과 다운로드 가능

### 5.4 결과 기록 예시

테스트 결과를 다음 형식으로 기록하세요:

```
### Test 1: Week1_Baseline_100VUser
- 테스트 일시: 2025-01-XX 14:00
- 가상 사용자: 100명
- Duration: 2분
- TPS: 450
- Peak TPS: 520
- Mean Test Time: 150ms
- 95 Percentile: 250ms
- 99 Percentile: 400ms
- Error Rate: 0%
- 병목: Thread Pool (Tomcat max threads: 200)

### 개선 아이디어:
- Thread Pool 크기 증가 (200 -> 400)
- 처리 시간 단축 (simulateProcessing 50ms -> 10ms)
```

---

## 6. 문제 해결

### 6.1 Agent를 찾을 수 없음 ("0 이하의 값으로 입력" 에러)

**원인**: Agent가 Controller에 연결되지 않음

**해결 방법**:

```bash
# 1. Agent 상태 확인
docker-compose ps

# 2. Agent 로그 확인
docker-compose logs ngrinder-agent | tail -20

# 3. Admin > Agent Management에서 Agent Approve

# 4. Agent 재시작
docker-compose restart ngrinder-agent

# 5. 1분 대기 후 웹 페이지 새로고침
```

### 6.2 "Connection Refused" 에러

**원인**: Spring Boot 애플리케이션이 실행되지 않음

**해결 방법**:

```bash
# 1. 애플리케이션 실행 확인
curl http://localhost:8080/api/v1/products/health

# 2. 실행되지 않았다면 시작
./gradlew bootRun

# 3. 8080 포트 사용 중이면
lsof -ti:8080 | xargs kill -9
./gradlew bootRun
```

### 6.3 "host.docker.internal" 접근 실패

**원인**: Docker에서 호스트 머신 접근 설정 문제

**해결 방법 1 - Docker Desktop 설정**:
1. Docker Desktop 열기
2. Settings > Resources > Network
3. "Enable host networking" 체크

**해결 방법 2 - IP 직접 사용**:

```bash
# Mac에서 호스트 IP 확인
ipconfig getifaddr en0

# 스크립트에서 host.docker.internal 대신 실제 IP 사용
# 예: http://192.168.1.100:8080/api/v1/products/{id}
```

### 6.4 Agent 수를 늘리고 싶을 때

```bash
# docker-compose.yml 수정 후
docker-compose up -d --scale ngrinder-agent=3

# 3개의 Agent가 시작됨
# 테스트 설정에서 Agent: 3으로 설정 가능
```

### 6.5 TPS가 예상보다 낮을 때

**확인 사항**:

1. **Spring Boot 로그 확인**
   ```bash
   # 에러나 경고 확인
   ./gradlew bootRun
   ```

2. **Thread Pool 설정 확인**
   - `application.yml`의 `server.tomcat.threads.max` 값

3. **DB Connection Pool 확인**
   ```bash
   # Actuator로 HikariCP 상태 확인
   curl http://localhost:8080/actuator/metrics/hikari.connections.active
   ```

4. **처리 시간 확인**
   - `ProductService`의 `simulateProcessing()` 시간

### 6.6 전체 재시작

모든 것이 작동하지 않을 때:

```bash
# 1. 모든 컨테이너 중지 및 삭제
docker-compose down -v

# 2. 애플리케이션 재시작
lsof -ti:8080 | xargs kill -9
./gradlew clean build
./gradlew bootRun &

# 3. nGrinder 재시작
docker-compose up -d ngrinder-controller ngrinder-agent

# 4. 3분 대기
sleep 180

# 5. 브라우저에서 http://localhost:8300 접속
```

---

## 7. 다음 단계로 가기 전 체크리스트

Week 1-2를 완료하기 전에 확인하세요:

- [ ] nGrinder에 로그인 성공
- [ ] Agent가 "Ready" 상태로 표시됨
- [ ] 스크립트 작성 및 검증 완료
- [ ] 100 VUser 테스트 성공적으로 실행
- [ ] TPS, 평균 응답시간 측정 완료
- [ ] 테스트 결과 기록
- [ ] 500 VUser, 1000 VUser로 확대 테스트
- [ ] 병목 지점 파악 (Thread Pool, DB 등)
- [ ] Thread Pool 조정 실험 완료
- [ ] 성능 개선 전후 비교 완료

---

## 8. 유용한 nGrinder 팁

### 8.1 여러 테스트 비교

1. **Performance Test** 목록에서
2. **비교하고 싶은 테스트들 선택**
3. **"Compare" 버튼 클릭**

### 8.2 스크립트 버전 관리

- 스크립트를 수정할 때마다 nGrinder가 자동으로 버전 저장
- **Script** 페이지에서 **"Revision" 탭**으로 이전 버전 확인 가능

### 8.3 실시간 Sampling

테스트 실행 중:
- **"Sampling" 탭** 클릭
- 개별 요청의 응답 시간, 상태 코드 확인
- 느린 요청 식별

### 8.4 태그 활용

- 테스트 생성 시 **Tag** 필드 활용
- 예: `week1`, `baseline`, `redis-before` 등
- 나중에 Tag로 필터링 가능

---

## 참고 자료

- **nGrinder 공식 문서**: http://naver.github.io/ngrinder/
- **Groovy 문법**: https://groovy-lang.org/documentation.html
- **Week 1-2 상세 가이드**: `WEEK1-2.md`

---

**이제 nGrinder로 본격적인 성능 테스트를 시작해보세요!** 🚀
