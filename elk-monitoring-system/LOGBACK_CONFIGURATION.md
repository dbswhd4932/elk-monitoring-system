# Logback 설정 상세 가이드

## 1. 로그 레벨별 Logstash 저장 설정 위치

### 📍 주요 설정 파일 위치

```
src/main/resources/logback-spring.xml  ← 여기서 모든 로그 레벨 설정!
```

---

## 2. 로그 레벨 설정 구조

### 2-1. Logger 레벨 설정 (어떤 로그를 기록할지)

**위치**: `logback-spring.xml` 하단부

```xml
<!-- 우리 애플리케이션 패키지 - DEBUG 레벨 이상 모두 기록 -->
<logger name="com.example.elkmonitoring" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>          <!-- 콘솔 출력 -->
    <appender-ref ref="ASYNC_JSON_FILE"/>  <!-- 파일 저장 -->
    <appender-ref ref="ASYNC_LOGSTASH"/>   <!-- Logstash 전송 ★ -->
    <appender-ref ref="ERROR_FILE"/>       <!-- 에러만 별도 저장 -->
</logger>

<!-- Root Logger - 나머지 모든 패키지는 INFO 레벨 이상만 -->
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="ASYNC_JSON_FILE"/>
    <appender-ref ref="ASYNC_LOGSTASH"/>   <!-- ★ 여기서 Logstash 전송! -->
</root>
```

**의미:**
- `level="DEBUG"`: DEBUG, INFO, WARN, ERROR 모두 기록
- `level="INFO"`: INFO, WARN, ERROR만 기록 (DEBUG는 무시)
- `level="WARN"`: WARN, ERROR만 기록
- `level="ERROR"`: ERROR만 기록

### 2-2. Appender 설정 (로그를 어디로 보낼지)

#### ① LOGSTASH Appender (Logstash로 전송)

```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5044</destination>  <!-- Logstash 주소:포트 -->

    <!-- 재연결 설정 -->
    <reconnectionDelay>10 second</reconnectionDelay>

    <!-- JSON 인코더 -->
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <!-- 커스텀 필드 추가 -->
        <customFields>{"app_name":"elk-monitoring-system","environment":"development"}</customFields>

        <!-- MDC 포함 (requestId, userId, clientIp) -->
        <includeMdc>true</includeMdc>
        <includeContext>true</includeContext>
        <includeCallerData>true</includeCallerData>
    </encoder>
</appender>
```

#### ② 비동기 Logstash Appender (성능 최적화)

```xml
<appender name="ASYNC_LOGSTASH" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="LOGSTASH"/>  <!-- 실제 LOGSTASH appender 참조 -->
    <queueSize>512</queueSize>      <!-- 큐 크기 -->
    <discardingThreshold>0</discardingThreshold>  <!-- 로그 버리지 않음 -->
</appender>
```

---

## 3. 로그 레벨별 동작 방식

### 현재 설정 기준

```
com.example.elkmonitoring 패키지:
  DEBUG → ✅ Logstash 전송
  INFO  → ✅ Logstash 전송
  WARN  → ✅ Logstash 전송
  ERROR → ✅ Logstash 전송 + ERROR_FILE에도 저장

다른 패키지 (Root Logger):
  DEBUG → ❌ 기록 안됨
  INFO  → ✅ Logstash 전송
  WARN  → ✅ Logstash 전송
  ERROR → ✅ Logstash 전송
```

### 예시 코드와 동작

```java
log.debug("디버그 메시지");  // DEBUG 레벨 → Logstash 전송 ✅
log.info("일반 정보");       // INFO 레벨 → Logstash 전송 ✅
log.warn("경고 메시지");     // WARN 레벨 → Logstash 전송 ✅
log.error("에러 발생");      // ERROR 레벨 → Logstash 전송 ✅ + error.log 저장 ✅
```

---

## 4. 특정 레벨만 Logstash로 보내는 방법

### 예: ERROR와 WARN만 Logstash로 전송하고 싶다면?

#### 방법 1: Logger 레벨 변경

```xml
<logger name="com.example.elkmonitoring" level="WARN" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="ASYNC_LOGSTASH"/>  <!-- WARN, ERROR만 전송됨 -->
</logger>
```

#### 방법 2: Filter 사용 (특정 레벨만 허용)

```xml
<appender name="ERROR_ONLY_LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5044</destination>

    <!-- ERROR만 허용하는 필터 -->
    <filter class="ch.qos.logback.classic.filter.LevelFilter">
        <level>ERROR</level>
        <onMatch>ACCEPT</onMatch>
        <onMismatch>DENY</onMismatch>
    </filter>

    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

#### 방법 3: Threshold Filter (특정 레벨 이상만)

```xml
<appender name="WARN_AND_ABOVE_LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5044</destination>

    <!-- WARN 이상만 (WARN, ERROR) -->
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>WARN</level>
    </filter>

    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

---

## 5. Logback 설정 파일 형식

### Q: logback-spring.xml은 꼭 XML로만 작성해야 하나요?

**답변: 아니요! 3가지 방식이 있습니다.**

### 5-1. XML 방식 (현재 사용 중) ✅

**파일명**: `logback-spring.xml` 또는 `logback.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

**장점:**
- ✅ 가장 일반적이고 문서화가 잘 되어있음
- ✅ IDE 자동완성 지원
- ✅ 많은 예제 코드 존재

**단점:**
- ❌ 장황함 (verbose)
- ❌ 가독성이 떨어질 수 있음

---

### 5-2. Groovy 방식 (간결함) 🎯

**파일명**: `logback.groovy`

```groovy
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import static ch.qos.logback.classic.Level.INFO

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    }
}

root(INFO, ["CONSOLE"])
```

**Logstash 전송 설정 (Groovy):**

```groovy
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import net.logstash.logback.appender.LogstashTcpSocketAppender
import net.logstash.logback.encoder.LogstashEncoder
import static ch.qos.logback.classic.Level.DEBUG

// Logstash Appender
appender("LOGSTASH", LogstashTcpSocketAppender) {
    destination = "localhost:5044"
    encoder(LogstashEncoder) {
        customFields = '{"app_name":"elk-monitoring-system"}'
        includeMdc = true
    }
}

// Logger 설정
logger("com.example.elkmonitoring", DEBUG, ["CONSOLE", "LOGSTASH"], false)
root(INFO, ["CONSOLE", "LOGSTASH"])
```

**장점:**
- ✅ 간결하고 읽기 쉬움
- ✅ 프로그래밍 언어처럼 사용 가능
- ✅ 조건문, 반복문 사용 가능

**단점:**
- ❌ Groovy 의존성 필요
- ❌ 상대적으로 예제가 적음

---

### 5-3. Java 코드 방식 (프로그래밍)

```java
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public class LogbackConfig {
    public static void configure() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} - %msg%n");
        encoder.start();

        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        ch.qos.logback.classic.Logger rootLogger = context.getLogger("ROOT");
        rootLogger.addAppender(consoleAppender);
        rootLogger.setLevel(Level.INFO);
    }
}
```

**장점:**
- ✅ 동적 설정 가능
- ✅ 타입 안정성

**단점:**
- ❌ 코드가 복잡해짐
- ❌ 일반적이지 않음

---

## 6. 권장 사항

### 현재 프로젝트에는 XML 방식 추천 ✅

**이유:**
1. Spring Boot가 기본으로 `logback-spring.xml` 지원
2. 많은 예제와 문서
3. Spring Profile별 설정 가능 (`<springProfile>` 태그)

### Spring Profile별 설정 예시

```xml
<configuration>
    <!-- 개발 환경 -->
    <springProfile name="dev">
        <logger name="com.example.elkmonitoring" level="DEBUG">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="ASYNC_LOGSTASH"/>
        </logger>
    </springProfile>

    <!-- 운영 환경 -->
    <springProfile name="prod">
        <logger name="com.example.elkmonitoring" level="INFO">
            <appender-ref ref="ASYNC_LOGSTASH"/>
        </logger>
    </springProfile>
</configuration>
```

---

## 7. 실전 예제

### 예제 1: 운영 환경에서는 ERROR만 Logstash로 전송

```xml
<springProfile name="prod">
    <!-- ERROR만 전송하는 Logstash Appender -->
    <appender name="PROD_LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>production-logstash:5044</destination>

        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>

        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="PROD_LOGSTASH"/>
    </root>
</springProfile>
```

### 예제 2: 비즈니스 이벤트만 별도 Logstash로 전송

```xml
<!-- 비즈니스 이벤트 전용 Logstash -->
<appender name="BUSINESS_EVENT_LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5045</destination>  <!-- 다른 포트 -->
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>

<!-- 특정 패키지만 -->
<logger name="com.example.elkmonitoring.service" level="INFO">
    <appender-ref ref="BUSINESS_EVENT_LOGSTASH"/>
</logger>
```

---

## 8. 요약

### Q1: log.info, warn, error가 Logstash에 저장되는 설정은 어디?
**답변:** `src/main/resources/logback-spring.xml`

- **Logger 레벨 설정** (94-111줄): 어떤 레벨을 기록할지
- **Appender 설정** (35-67줄): Logstash로 어떻게 전송할지
- **Appender 연결** (94-111줄): Logger에 Appender 연결

### Q2: logback-spring.xml은 꼭 XML로만 작성해야 하나?
**답변:** 아니요!

- ✅ **XML** (추천): 일반적이고 문서화 잘됨
- ✅ **Groovy**: 간결하고 읽기 쉬움
- ✅ **Java 코드**: 동적 설정 가능

**현재 프로젝트는 XML 방식 사용 중이며, 이것이 가장 권장되는 방식입니다.**

---

## 9. 추가 학습 리소스

- [Logback 공식 문서](https://logback.qos.ch/manual/configuration.html)
- [Logstash Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
