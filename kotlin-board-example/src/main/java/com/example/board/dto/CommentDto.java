package com.example.board.dto;

import com.example.board.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CommentDto {

    // 댓글 생성 요청 DTO
    public static class CreateCommentRequest {
        @NotBlank(message = "내용은 필수입니다")
        private String content;

        @NotBlank(message = "작성자는 필수입니다")
        @Size(max = 50, message = "작성자는 50자 이하여야 합니다")
        private String author;

        public CreateCommentRequest() {
        }

        public CreateCommentRequest(String content, String author) {
            this.content = content;
            this.author = author;
        }

        public Comment toEntity() {
            return new Comment(content, author);
        }

        // Getter & Setter
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

    // 댓글 수정 요청 DTO
    public static class UpdateCommentRequest {
        @NotBlank(message = "내용은 필수입니다")
        private String content;

        public UpdateCommentRequest() {
        }

        public UpdateCommentRequest(String content) {
            this.content = content;
        }

        // Getter & Setter
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    // 댓글 응답 DTO
    public static class CommentResponse {
        private Long id;
        private String content;
        private String author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long postId;

        public CommentResponse() {
        }

        public CommentResponse(Long id, String content, String author,
                              LocalDateTime createdAt, LocalDateTime updatedAt, Long postId) {
            this.id = id;
            this.content = content;
            this.author = author;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.postId = postId;
        }

        public static CommentResponse from(Comment comment) {
            return new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    comment.getAuthor(),
                    comment.getCreatedAt(),
                    comment.getUpdatedAt(),
                    comment.getPost() != null ? comment.getPost().getId() : null
            );
        }

        // Getter & Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }
    }
}
