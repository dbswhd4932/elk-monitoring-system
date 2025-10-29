# Java to Kotlin 변환 가이드

이 문서는 Java로 작성된 게시판 코드를 Kotlin으로 변환하는 단계별 가이드입니다.

## 목표

Java 코드를 보면서 직접 손으로 Kotlin으로 변환해보며 Kotlin 문법을 익히세요!

## 변환 순서

1. Entity 클래스 변환 (가장 기본이 되는 클래스)
2. DTO 변환 (data class 활용)
3. Repository 변환 (인터페이스는 거의 동일)
4. Service 변환 (핵심 로직, Kotlin 스타일 적용)
5. Controller 변환
6. Application 변환 (가장 간단)

---

## 1. Entity 변환

### Post.java → Post.kt 변환 체크리스트

#### 현재 Java 코드 위치
`src/main/java/com/example/board/entity/Post.java`

#### 변환할 Kotlin 파일 위치
`src/main/kotlin/com/example/board/entity/Post.kt`

#### 변환 포인트

**1단계: 기본 구조**
```kotlin
// Java의 public class -> Kotlin의 class (기본이 public)
@Entity
@Table(name = "posts")
data class Post(  // data class 사용!
    // ...
)
```

**2단계: 필드 → 프로퍼티**
```kotlin
// Java:
// private Long id;
// public Long getId() { return id; }
// public void setId(Long id) { this.id = id; }

// Kotlin:
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
val id: Long? = null  // nullable, 불변
```

**3단계: 생성자**
```kotlin
// Java의 생성자 파라미터를 클래스 헤더로 이동
data class Post(
    val id: Long? = null,
    var title: String,      // var = 변경 가능
    var content: String,
    var author: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @OneToMany(...)
    val comments: MutableList<Comment> = mutableListOf()
)
```

**4단계: 메서드는 클래스 본문에**
```kotlin
data class Post(...) {
    fun addComment(comment: Comment) {
        comments.add(comment)
        comment.post = this
    }

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }
}
```

**주의사항:**
- `Long?` vs `Long`: nullable 타입은 `?` 붙이기
- `val` vs `var`: 불변은 val, 가변은 var
- `MutableList` vs `List`: 수정 가능한 리스트는 MutableList

---

## 2. DTO 변환

### PostDto.java → PostDto.kt 변환 체크리스트

#### 변환 포인트

**1단계: 내부 클래스를 data class로**
```kotlin
// Java의 static class -> Kotlin의 일반 클래스
data class CreatePostRequest(
    @field:NotBlank(message = "제목은 필수입니다")  // @field: 주의!
    val title: String,

    @field:NotBlank(message = "내용은 필수입니다")
    val content: String,

    @field:NotBlank(message = "작성자는 필수입니다")
    val author: String
) {
    fun toEntity(): Post {
        return Post(
            title = title,    // named argument
            content = content,
            author = author
        )
    }
}
```

**2단계: companion object로 static 메서드**
```kotlin
data class PostResponse(...) {
    companion object {
        fun from(post: Post): PostResponse {
            return PostResponse(
                id = post.id!!,  // !! = non-null assertion
                title = post.title,
                // ...
            )
        }
    }
}
```

**주의사항:**
- Validation 어노테이션은 `@field:`를 붙여야 함
- `static` → `companion object`
- getter/setter 자동 생성되므로 불필요

---

## 3. Repository 변환

### PostRepository.java → PostRepository.kt

#### 변환 포인트

```kotlin
interface PostRepository : JpaRepository<Post, Long> {
    // Java와 거의 동일
    fun findByAuthor(author: String): List<Post>

    // 반환 타입을 nullable로 변경
    fun findByIdWithComments(id: Long): Post?  // Optional 대신 nullable

    // @Query는 """ 사용 (multiline string)
    @Query("""
        SELECT p FROM Post p
        WHERE p.title LIKE %:keyword%
    """)
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Post>
}
```

**주의사항:**
- `Optional<Post>` → `Post?`
- `:` 로 상속 표현

---

## 4. Service 변환 (중요!)

### PostService.java → PostService.kt

#### 변환 포인트

**1단계: 생성자 주입 간소화**
```kotlin
// Java:
// private final PostRepository postRepository;
// public PostService(PostRepository postRepository) {
//     this.postRepository = postRepository;
// }

// Kotlin:
@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository  // 한 줄로 끝!
) {
```

**2단계: Optional 처리 → Elvis 연산자**
```kotlin
// Java:
// Post post = postRepository.findById(id)
//     .orElseThrow(() -> new IllegalArgumentException("..."));

// Kotlin:
val post = postRepository.findByIdOrNull(id)
    ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")
```

**3단계: Stream API → 컬렉션 함수**
```kotlin
// Java:
// page.getContent().stream()
//     .map(PostResponse::from)
//     .collect(Collectors.toList())

// Kotlin:
page.content.map { PostResponse.from(it) }
```

**4단계: 문자열 보간**
```kotlin
// Java:
// "게시글을 찾을 수 없습니다. id: " + id

// Kotlin:
"게시글을 찾을 수 없습니다. id: $id"
```

---

## 5. Controller 변환

### PostController.java → PostController.kt

#### 변환 포인트

**1단계: 클래스 선언**
```kotlin
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
```

**2단계: 메서드 선언**
```kotlin
// Java:
// public ResponseEntity<PostListResponse> getPosts(...)

// Kotlin:
fun getPosts(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "10") size: Int
): ResponseEntity<PostListResponse> {
```

**3단계: if 표현식 활용**
```kotlin
val sort = if (direction.uppercase() == "DESC") {
    Sort.by(sortBy).descending()
} else {
    Sort.by(sortBy).ascending()
}
```

**주의사항:**
- `Void` → `Unit` (생략 가능)
- `int` → `Int`, `long` → `Long`

---

## 6. Application 변환

### BoardApplication.java → BoardApplication.kt

#### 변환 포인트

```kotlin
@SpringBootApplication
class BoardApplication

fun main(args: Array<String>) {
    runApplication<BoardApplication>(*args)
}
```

**주요 변경점:**
- main 메서드를 클래스 밖으로
- `SpringApplication.run()` → `runApplication<>()`
- `*args`: spread operator (Java의 `...args`)

---

## 변환 실습 가이드

### 추천 순서

1. **먼저 직접 변환해보기**
   - Java 파일을 보면서
   - 새로운 Kotlin 파일 생성
   - 위 가이드 참고하며 직접 타이핑

2. **IntelliJ 자동 변환 기능 활용**
   - Java 파일 내용 복사
   - Kotlin 파일에 붙여넣기
   - IntelliJ가 자동 변환 제안
   - 코드 리뷰하며 개선

3. **실행 및 테스트**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

4. **API 테스트**
   ```bash
   curl -X POST http://localhost:8080/api/posts \
     -H "Content-Type: application/json" \
     -d '{"title":"테스트","content":"내용","author":"작성자"}'
   ```

---

## 변환 체크리스트

### Entity (Post, Comment)
- [ ] class → data class
- [ ] 필드 → 프로퍼티 (val/var)
- [ ] nullable 타입 처리 (?)
- [ ] getter/setter 제거
- [ ] equals/hashCode 확인

### DTO
- [ ] static class → companion object
- [ ] data class 활용
- [ ] @field: 어노테이션
- [ ] getter/setter 제거

### Repository
- [ ] interface 상속 (:)
- [ ] Optional → nullable (?)
- [ ] """ multiline string

### Service
- [ ] 생성자 주입 간소화
- [ ] Optional → Elvis (?:)
- [ ] Stream → map/filter
- [ ] 문자열 보간 ($)

### Controller
- [ ] fun 키워드
- [ ] Int, Long 타입
- [ ] if 표현식
- [ ] Void → Unit

### Application
- [ ] main 함수 클래스 밖으로
- [ ] runApplication<>()

---

## 자주 하는 실수

### 1. Validation 어노테이션
```kotlin
// ❌ 잘못된 예
@NotBlank
val title: String

// ✅ 올바른 예
@field:NotBlank
val title: String
```

### 2. Nullable 처리
```kotlin
// ❌ NPE 위험
val post = postRepository.findById(id).get()

// ✅ Kotlin 스타일
val post = postRepository.findByIdOrNull(id)
    ?: throw IllegalArgumentException("...")
```

### 3. 불변/가변 혼동
```kotlin
// ❌ Entity의 수정 가능한 필드를 val로
val title: String  // 수정 불가!

// ✅ 수정 가능한 필드는 var
var title: String
```

### 4. MutableList vs List
```kotlin
// ❌ 수정 불가
val comments: List<Comment> = listOf()

// ✅ 수정 가능
val comments: MutableList<Comment> = mutableListOf()
```

---

## 디렉토리 구조

### 현재 (Java)
```
src/main/java/com/example/board/
├── entity/
├── dto/
├── repository/
├── service/
├── controller/
└── BoardApplication.java
```

### 변환 후 (Kotlin)
```
src/main/kotlin/com/example/board/
├── entity/
├── dto/
├── repository/
├── service/
├── controller/
└── BoardApplication.kt
```

---

## 학습 팁

1. **한 파일씩 변환**: 전체를 한번에 하지 말고 하나씩
2. **빌드 확인**: 각 파일 변환 후 빌드되는지 확인
3. **테스트 실행**: 기능이 정상 동작하는지 확인
4. **코드 비교**: Java와 Kotlin 코드를 나란히 놓고 비교
5. **KOTLIN_GUIDE.md 참고**: 문법이 헷갈리면 가이드 참고

---

## 변환 후 빌드 설정

build.gradle.kts가 이미 Kotlin을 지원하도록 설정되어 있습니다:

```kotlin
plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
}
```

단, Java와 Kotlin을 같이 사용할 수도 있으므로 원하는 만큼만 변환하셔도 됩니다!

---

## 참고 자료

- **KOTLIN_GUIDE.md**: Java vs Kotlin 상세 비교
- **README.md**: 프로젝트 전체 개요
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [IntelliJ 자동 변환](https://www.jetbrains.com/help/idea/converting-a-java-file-to-kotlin-file.html)

---

## 변환 완료 후

모든 Java 파일을 Kotlin으로 변환했다면:

1. Java 디렉토리 삭제
   ```bash
   rm -rf src/main/java src/test/java
   ```

2. 빌드 및 테스트
   ```bash
   ./gradlew clean build
   ./gradlew test
   ```

3. 실행
   ```bash
   ./gradlew bootRun
   ```

화이팅! 🚀
