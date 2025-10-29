package com.example.board.service;

import com.example.board.dto.PostDto.*;
import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    public PostListResponse getPosts(Pageable pageable) {
        Page<Post> page = postRepository.findAll(pageable);

        return new PostListResponse(
                page.getContent().stream()
                        .map(PostResponse::from)
                        .collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    /**
     * 게시글 상세 조회
     */
    public PostDetailResponse getPost(Long id) {
        Post post = postRepository.findByIdWithComments(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id));

        return PostDetailResponse.from(post);
    }

    /**
     * 게시글 검색
     */
    public PostListResponse searchPosts(String keyword, Pageable pageable) {
        Page<Post> page = postRepository.searchByKeyword(keyword, pageable);

        return new PostListResponse(
                page.getContent().stream()
                        .map(PostResponse::from)
                        .collect(Collectors.toList()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public PostResponse createPost(CreatePostRequest request) {
        Post post = request.toEntity();
        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long id, UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id));

        // Dirty Checking을 통한 업데이트
        post.update(request.getTitle(), request.getContent());

        return PostResponse.from(post);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id);
        }

        postRepository.deleteById(id);
    }
}
