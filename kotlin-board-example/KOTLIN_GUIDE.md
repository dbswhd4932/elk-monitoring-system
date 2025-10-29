# Java 개발자를 위한 Kotlin 가이드

## 목차
1. [기본 문법](#1-기본-문법)
2. [Null Safety](#2-null-safety)
3. [클래스와 객체](#3-클래스와-객체)
4. [함수형 프로그래밍](#4-함수형-프로그래밍)
5. [스코프 함수](#5-스코프-함수)
6. [확장 함수](#6-확장-함수)
7. [Spring Boot와 함께 사용하기](#7-spring-boot와-함께-사용하기)

---

## 1. 기본 문법

### 1.1 변수 선언

**Java:**
```java
final String name = "John";  // 불변
int age = 30;                 // 가변
Long id = null;               // nullable
```

**Kotlin:**
```kotlin
val name = "John"      // 불변 (final) - 타입 추론
var age = 30           // 가변
val id: Long? = null   // nullable 명시
```

**핵심 차이점:**
- `val` = value (불변, Java의 final)
- `var` = variable (가변)
- 타입 추론: 컴파일러가 자동으로 타입 결정
- `?`: nullable 타입 명시적 표현

### 1.2 함수 선언

**Java:**
```java
public String greet(String name) {
    return "Hello, " + name;
}

public void printMessage(String msg) {
    System.out.println(msg);
}
```

**Kotlin:**
```kotlin
// 일반 함수
fun greet(name: String): String {
    return "Hello, $name"  // 문자열 보간
}

// 단일 표현식 함수
fun greet(name: String) = "Hello, $name"

// Unit = Java의 void
fun printMessage(msg: String): Unit {
    println(msg)
}

// Unit 생략 가능
fun printMessage(msg: String) {
    println(msg)
}

// 기본 파라미터
fun greet(name: String = "Guest") = "Hello, $name"
```

### 1.3 조건문

**Java:**
```java
String result;
if (score >= 90) {
    result = "A";
} else if (score >= 80) {
    result = "B";
} else {
    result = "C";
}
```

**Kotlin:**
```kotlin
// if는 표현식 (expression)
val result = if (score >= 90) {
    "A"
} else if (score >= 80) {
    "B"
} else {
    "C"
}

// when (Java의 switch와 유사하지만 더 강력)
val result = when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    score >= 70 -> "C"
    else -> "F"
}

// when with argument
val result = when(score) {
    100 -> "Perfect!"
    in 90..99 -> "A"
    in 80..89 -> "B"
    else -> "C"
}
```

---

## 2. Null Safety

Kotlin의 가장 큰 특징 중 하나는 컴파일 타임에 NPE를 방지한다는 것입니다.

### 2.1 Nullable vs Non-Nullable

**Java:**
```java
String name = null;  // 컴파일 OK, 런타임 NPE 위험
name.length();       // NullPointerException!
```

**Kotlin:**
```kotlin
val name: String = null   // 컴파일 에러!
val name: String? = null  // OK

// Safe Call
val length = name?.length  // null이면 null 반환

// Elvis 연산자
val length = name?.length ?: 0  // null이면 0 반환

// Non-null assertion (주의!)
val length = name!!.length  // null이면 NPE 발생
```

### 2.2 실전 예제

**Java:**
```java
public PostResponse getPost(Long id) {
    Post post = postRepository.findById(id).orElse(null);
    if (post == null) {
        throw new IllegalArgumentException("게시글을 찾을 수 없습니다");
    }
    return PostResponse.from(post);
}
```

**Kotlin:**
```kotlin
fun getPost(id: Long): PostResponse {
    val post = postRepository.findByIdOrNull(id)
        ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")

    return PostResponse.from(post)
}

// 또는
fun getPost(id: Long): PostResponse {
    return postRepository.findByIdOrNull(id)
        ?.let { PostResponse.from(it) }
        ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")
}
```

---

## 3. 클래스와 객체

### 3.1 데이터 클래스

**Java:**
```java
public class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // getter, setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... (20줄 이상)

    @Override
    public boolean equals(Object o) { /* ... */ }

    @Override
    public int hashCode() { /* ... */ }

    @Override
    public String toString() { /* ... */ }
}
```

**Kotlin:**
```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String
)
// equals, hashCode, toString, copy, componentN 자동 생성!

// 사용 예
val user = User(1L, "John", "john@example.com")
val copy = user.copy(name = "Jane")  // copy 메서드
```

### 3.2 생성자

**Java:**
```java
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired  // 또는 생성자에 @RequiredArgsConstructor
    public PostService(PostRepository postRepository,
                      CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }
}
```

**Kotlin:**
```kotlin
@Service
class PostService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) {
    // 생성자 파라미터에서 바로 프로퍼티 선언!
}
```

### 3.3 상속과 오버라이드

**Java:**
```java
public class Animal {
    public void sound() {
        System.out.println("Some sound");
    }
}

public class Dog extends Animal {
    @Override
    public void sound() {
        System.out.println("Bark!");
    }
}
```

**Kotlin:**
```kotlin
// Kotlin의 클래스는 기본적으로 final
// 상속 가능하게 하려면 open 키워드 필요
open class Animal {
    open fun sound() {
        println("Some sound")
    }
}

class Dog : Animal() {
    override fun sound() {
        println("Bark!")
    }
}
```

---

## 4. 함수형 프로그래밍

### 4.1 컬렉션 변환

**Java:**
```java
List<Post> posts = postRepository.findAll();

// Stream API
List<PostResponse> responses = posts.stream()
    .map(PostResponse::from)
    .collect(Collectors.toList());

List<PostResponse> filtered = posts.stream()
    .filter(post -> post.getAuthor().equals("John"))
    .map(PostResponse::from)
    .collect(Collectors.toList());
```

**Kotlin:**
```kotlin
val posts = postRepository.findAll()

// 간결한 컬렉션 함수
val responses = posts.map { PostResponse.from(it) }

val filtered = posts
    .filter { it.author == "John" }
    .map { PostResponse.from(it) }

// it: 람다의 단일 파라미터 (implicit parameter)
```

### 4.2 주요 컬렉션 함수

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// map: 변환
numbers.map { it * 2 }  // [2, 4, 6, 8, 10]

// filter: 필터링
numbers.filter { it > 3 }  // [4, 5]

// find: 첫 번째 매칭 요소
numbers.find { it > 3 }  // 4

// any: 하나라도 만족하는가?
numbers.any { it > 10 }  // false

// all: 모두 만족하는가?
numbers.all { it > 0 }  // true

// groupBy: 그룹화
val posts = listOf(...)
posts.groupBy { it.author }  // Map<String, List<Post>>

// associate: Map 변환
posts.associate { it.id to it.title }  // Map<Long, String>

// flatMap: 중첩 컬렉션 평탄화
posts.flatMap { it.comments }  // List<Comment>
```

---

## 5. 스코프 함수

Kotlin의 독특하고 강력한 기능입니다.

### 5.1 let

- 주로 null 체크 후 실행할 때 사용
- 반환: 람다의 결과

```kotlin
// Java
Post post = postRepository.findById(id).orElse(null);
if (post != null) {
    return PostResponse.from(post);
}

// Kotlin
postRepository.findByIdOrNull(id)?.let {
    return PostResponse.from(it)
}
```

### 5.2 apply

- 객체 초기화할 때 주로 사용
- 반환: 객체 자신

```kotlin
// Java
Post post = new Post();
post.setTitle("제목");
post.setContent("내용");
post.setAuthor("작성자");

// Kotlin
val post = Post().apply {
    title = "제목"
    content = "내용"
    author = "작성자"
}
```

### 5.3 also

- 객체를 사용하고 객체를 반환
- 로깅이나 부가 작업에 유용

```kotlin
val savedPost = postRepository.save(post).also {
    logger.info("Saved post: ${it.id}")
}
```

### 5.4 run

- 객체의 메서드 호출 후 결과 반환

```kotlin
val result = post.run {
    update(title, content)
    PostResponse.from(this)
}
```

### 5.5 with

- 객체를 파라미터로 받음

```kotlin
val response = with(post) {
    PostResponse(
        id = id!!,
        title = title,
        content = content,
        author = author
    )
}
```

---

## 6. 확장 함수

기존 클래스에 새로운 메서드를 추가할 수 있습니다.

```kotlin
// String에 이메일 검증 함수 추가
fun String.isEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

// 사용
val email = "test@example.com"
if (email.isEmail()) {
    println("Valid email")
}

// Spring Data JPA의 확장 함수 예
// findById(id).orElse(null) -> findByIdOrNull(id)
postRepository.findByIdOrNull(id)
```

---

## 7. Spring Boot와 함께 사용하기

### 7.1 필수 플러그인

```kotlin
plugins {
    kotlin("plugin.spring")  // @Component 등을 open class로
    kotlin("plugin.jpa")     // @Entity를 open class로
}
```

**왜 필요한가?**
- Kotlin의 클래스는 기본적으로 final
- Spring AOP와 JPA는 프록시 생성을 위해 상속 필요
- 이 플러그인들이 자동으로 open class로 변환

### 7.2 JPA Entity 작성 팁

```kotlin
@Entity
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,  // nullable로 선언 (DB가 생성)

    var title: String,     // 변경 가능한 필드는 var

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL])
    val comments: MutableList<Comment> = mutableListOf()
) {
    // equals/hashCode는 id 기반으로 재정의 권장
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Post
        return id != null && id == other.id
    }

    override fun hashCode() = id?.hashCode() ?: 0
}
```

### 7.3 Repository

```kotlin
interface PostRepository : JpaRepository<Post, Long> {
    // Query Method는 Java와 동일
    fun findByAuthor(author: String): List<Post>

    // Kotlin의 nullable 반환
    fun findByIdOrNull(id: Long): Post?
}
```

### 7.4 Service

```kotlin
@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository
) {
    fun getPost(id: Long): PostResponse {
        val post = postRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")

        return PostResponse.from(post)
    }

    @Transactional
    fun createPost(request: CreatePostRequest): PostResponse {
        val post = request.toEntity()
        val savedPost = postRepository.save(post)
        return PostResponse.from(savedPost)
    }
}
```

### 7.5 Controller

```kotlin
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PostListResponse> {
        val response = postService.getPosts(
            PageRequest.of(page, size)
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createPost(
        @Valid @RequestBody request: CreatePostRequest
    ): ResponseEntity<PostResponse> {
        val response = postService.createPost(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
```

---

## 실전 팁

### 1. Java 코드와 호환

Kotlin은 Java와 100% 호환됩니다. 같은 프로젝트에서 Java와 Kotlin을 섞어 사용 가능합니다.

### 2. IntelliJ 자동 변환

IntelliJ IDEA는 Java 코드를 Kotlin으로 자동 변환해줍니다:
- Java 파일 복사 → Kotlin 파일에 붙여넣기
- Code → Convert Java File to Kotlin File

### 3. 점진적 마이그레이션

기존 Java 프로젝트를 Kotlin으로 전환할 때:
1. 새로운 클래스는 Kotlin으로 작성
2. 기존 Java 클래스는 그대로 유지
3. 필요할 때만 Java → Kotlin 변환

### 4. 학습 순서 추천

1. 기본 문법 (변수, 함수, 클래스)
2. Null Safety
3. 데이터 클래스와 프로퍼티
4. 컬렉션 함수
5. 스코프 함수
6. 확장 함수
7. 코루틴 (비동기 프로그래밍)

---

## 추가 학습 자료

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Playground](https://play.kotlinlang.org/) - 브라우저에서 실습
- [Kotlin Koans](https://play.kotlinlang.org/koans) - 인터랙티브 학습
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
