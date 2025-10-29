package com.example.board.dto;

import com.example.board.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDto {

    // 게시글 생성 요청 DTO
    public static class CreatePostRequest {
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        private String title;

        @NotBlank(message = "내용은 필수입니다")
        private String content;

        @NotBlank(message = "작성자는 필수입니다")
        @Size(max = 50, message = "작성자는 50자 이하여야 합니다")
        private String author;

        public CreatePostRequest() {
        }

        public CreatePostRequest(String title, String content, String author) {
            this.title = title;
            this.content = content;
            this.author = author;
        }

        public Post toEntity() {
            return new Post(title, content, author);
        }

        // Getter & Setter
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }

    // 게시글 수정 요청 DTO
    public static class UpdatePostRequest {
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        private String title;

        @NotBlank(message = "내용은 필수입니다")
        private String content;

        public UpdatePostRequest() {
        }

        public UpdatePostRequest(String title, String content) {
            this.title = title;
            this.content = content;
        }

        // Getter & Setter
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    // 게시글 응답 DTO
    public static class PostResponse {
        private Long id;
        private String title;
        private String content;
        private String author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int commentCount;

        public PostResponse() {
        }

        public PostResponse(Long id, String title, String content, String author,
                           LocalDateTime createdAt, LocalDateTime updatedAt, int commentCount) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = author;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.commentCount = commentCount;
        }

        public static PostResponse from(Post post) {
            return new PostResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getAuthor(),
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    post.getComments().size()
            );
        }

        // Getter & Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }
    }

    // 게시글 상세 응답 DTO
    public static class PostDetailResponse {
        private Long id;
        private String title;
        private String content;
        private String author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<CommentDto.CommentResponse> comments;

        public PostDetailResponse() {
        }

        public PostDetailResponse(Long id, String title, String content, String author,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 List<CommentDto.CommentResponse> comments) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = author;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.comments = comments;
        }

        public static PostDetailResponse from(Post post) {
            return new PostDetailResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getAuthor(),
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    post.getComments().stream()
                            .map(CommentDto.CommentResponse::from)
                            .collect(Collectors.toList())
            );
        }

        // Getter & Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public List<CommentDto.CommentResponse> getComments() {
            return comments;
        }

        public void setComments(List<CommentDto.CommentResponse> comments) {
            this.comments = comments;
        }
    }

    // 게시글 목록 응답 DTO
    public static class PostListResponse {
        private List<PostResponse> posts;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int size;

        public PostListResponse() {
        }

        public PostListResponse(List<PostResponse> posts, long totalElements,
                               int totalPages, int currentPage, int size) {
            this.posts = posts;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }

        // Getter & Setter
        public List<PostResponse> getPosts() {
            return posts;
        }

        public void setPosts(List<PostResponse> posts) {
            this.posts = posts;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
