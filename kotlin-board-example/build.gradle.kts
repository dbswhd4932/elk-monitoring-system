import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"  // Spring용 Kotlin 플러그인 (Java의 @Component 등을 open class로 만들어줌)
    kotlin("plugin.jpa") version "1.9.20"     // JPA용 Kotlin 플러그인 (Entity를 open class로 만들어줌)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin 관련 의존성
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")  // JSON 직렬화/역직렬화를 위한 Kotlin 모듈
    implementation("org.jetbrains.kotlin:kotlin-reflect")  // Kotlin 리플렉션 (Spring에서 필요)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")  // Kotlin 표준 라이브러리

    // H2 Database (개발용)
    runtimeOnly("com.h2database:h2")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"  // Java의 @Nullable/@Nonnull 어노테이션 지원
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
