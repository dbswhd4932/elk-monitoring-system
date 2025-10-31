package com.example.board.service

import com.example.board.dto.PostDtoKt
import com.example.board.repository.PostRepositoryKt
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PostServiceKt(
    private val postRepositoryKt: PostRepositoryKt
) {
    // 게시글 목족 조회 (페이징)
    fun getPosts(pageable: Pageable): PostDtoKt.PostListResponse {
        val page = postRepositoryKt.findAll(pageable)

        return PostDtoKt.PostListResponse(
            posts = page.content.map {
                PostDtoKt.PostResponse.from(it) // it -> 현재요소
            },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            size = page.size
        )
    }

    // 게시글 상세 조회
    fun getPost(id: Long): PostDtoKt.PostDetailResponse {
        val post = postRepositoryKt.findByIdWithComments(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. Id: " + id)

        return PostDtoKt.PostDetailResponse.from(post)
    }

    // 게시글 검색
    fun searchPost(keyword: String, pageable: Pageable): PostDtoKt.PostListResponse {
        val page = postRepositoryKt.searchByKeyword(keyword, pageable)

        return PostDtoKt.PostListResponse(
            posts = page.content.map {
                PostDtoKt.PostResponse.from(it)
            },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            size = page.size
        )
    }
}