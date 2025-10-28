# 🚀 High Traffic Mastery - 시작 가이드

**축하합니다!** 대용량 트래픽 처리 학습 프로젝트가 성공적으로 설정되었습니다.

## ✅ 현재 상태

- ✅ Spring Boot API 서버 구축 완료
- ✅ 100개 상품 데이터 자동 생성
- ✅ H2 Database 설정 완료
- ✅ Actuator 모니터링 엔드포인트 활성화
- ✅ Docker Compose 환경 구성 완료

## 🎯 첫 번째 학습 단계: Week 1-2

### 1단계: 애플리케이션 실행 확인

애플리케이션이 현재 실행 중입니다! 아래 명령어로 확인해보세요:

```bash
# Health Check
curl http://localhost:8080/api/v1/products/health
# 응답: OK

# 상품 조회 테스트
curl http://localhost:8080/api/v1/products/1
# 응답: {"id":1,"name":"상품 1","price":1000, ...}

# 전체 상품 조회
curl http://localhost:8080/api/v1/products
```

### 2단계: nGrinder 환경 구성

터미널을 새로 열어서 다음 명령어를 실행하세요:

```bash
cd high-traffic-mastery

# nGrinder 컨테이너 시작
docker-compose up -d ngrinder-controller ngrinder-agent

# 로그 확인 (완전히 시작될 때까지 1-2분 소요)
docker-compose logs -f ngrinder-controller

# Ctrl+C로 로그 종료
```

**nGrinder 접속**:
- URL: http://localhost:8300
- 기본 계정: `admin / admin`

### 3단계: 첫 번째 부하 테스트

**⭐ nGrinder를 처음 사용한다면 반드시 읽어보세요!**

- **[NGRINDER_GUIDE.md](NGRINDER_GUIDE.md)** - nGrinder 완벽 가이드 (Agent 설정, 스크립트 작성, 테스트 실행, 결과 분석)

**자세한 학습 가이드는 `WEEK1-2.md` 파일을 참조하세요!**

```bash
# nGrinder 완벽 가이드 읽기 (처음 사용자 필수!)
cat NGRINDER_GUIDE.md

# Week 1-2 학습 가이드 열기
cat WEEK1-2.md
```

---

## 📂 프로젝트 구조

```
high-traffic-mastery/
├── README.md                    # 전체 프로젝트 개요
├── START_HERE.md               # 이 파일 (시작 가이드)
├── WEEK1-2.md                  # Week 1-2 학습 가이드
├── docker-compose.yml          # 모든 인프라 서비스 설정
├── build.gradle                # Gradle 빌드 설정
├── src/
│   └── main/
│       ├── java/com/traffic/mastery/
│       │   ├── HighTrafficMasteryApplication.java
│       │   ├── config/
│       │   │   └── DataInitializer.java        # 초기 데이터 생성
│       │   └── week1/                           # Week 1-2 코드
│       │       ├── domain/Product.java
│       │       ├── repository/ProductRepository.java
│       │       ├── service/ProductService.java
│       │       ├── dto/ProductResponse.java
│       │       └── controller/ProductController.java
│       └── resources/
│           └── application.yml                   # 설정 파일
└── monitoring/
    └── prometheus.yml          # Prometheus 설정
```

---

## 🛠️ 유용한 명령어

### 애플리케이션 관리

```bash
# 애플리케이션 실행
./gradlew bootRun

# 빌드
./gradlew clean build

# 포트 8080 사용 프로세스 종료
lsof -ti:8080 | xargs kill -9
```

### Docker 서비스 관리

```bash
# 모든 서비스 시작
docker-compose up -d

# 특정 서비스만 시작 (예: nGrinder)
docker-compose up -d ngrinder-controller ngrinder-agent

# 서비스 종료
docker-compose down

# 로그 확인
docker-compose logs -f [서비스명]

# 실행 중인 컨테이너 확인
docker-compose ps
```

---

## 📊 주차별 학습 로드맵

| Week | 주제 | 핵심 기술 | 목표 TPS |
|------|------|----------|----------|
| **1-2** | **기본 성능 측정** | nGrinder, JMeter | 500+ |
| 3-4 | Redis 캐싱 | Look-Aside, Write-Through | 2000+ |
| 5-6 | Kafka 비동기 처리 | Producer/Consumer | 5000+ |
| 7-8 | DB 최적화 | 인덱싱, Connection Pool | 8000+ |
| 9-11 | 통합 프로젝트 | 티켓팅, 플래시세일 | 10000+ |
| 12 | 모니터링 & 튜닝 | Prometheus, Grafana | - |

**현재 단계: Week 1-2**

---

##  다음 단계

1. **WEEK1-2.md 파일 읽기** - 상세한 학습 가이드
2. **nGrinder 설정** - 웹 UI에서 스크립트 작성
3. **첫 번째 부하 테스트 실행** - 100 VUser로 시작
4. **결과 분석** - TPS, 응답시간, 병목 지점 파악
5. **성능 실험** - Thread Pool 조정, 처리 시간 변경

---

## 🆘 문제 해결

### 애플리케이션이 시작 안됨
```bash
# 포트 충돌 확인
lsof -i:8080

# 충돌 프로세스 종료
lsof -ti:8080 | xargs kill -9

# 재시작
./gradlew bootRun
```

### nGrinder 접속 안됨
```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs ngrinder-controller

# 재시작
docker-compose restart ngrinder-controller
```

### Redis/Kafka 에러 발생
Week 1-2에서는 Redis와 Kafka를 사용하지 않습니다.
`build.gradle`에서 해당 의존성이 주석 처리되어 있는지 확인하세요.

---

## 📚 추가 리소스

- **전체 가이드**: `README.md`
- **Week 1-2 상세 가이드**: `WEEK1-2.md`
- **nGrinder 공식 문서**: http://naver.github.io/ngrinder/
- **Spring Boot Actuator**: http://localhost:8080/actuator
- **H2 Console**: http://localhost:8080/h2-console

---

**Good luck with your learning! 🎓**

질문이 있으면 `README.md`를 참조하거나 각 주차별 가이드를 확인하세요.
