package com.example.board.controller

import com.example.board.dto.PostDtoKt
import com.example.board.service.PostServiceKt
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts/kt")
class PostControllerKt(

    private val postServiceKt: PostServiceKt

) {
    // 게시글 목록 조회
    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String,
    ): ResponseEntity<PostDtoKt.PostListResponse> {

        val sort = if (direction.equals("DESC", ignoreCase = true)) // true -> 대소문자 무시
            Sort.by(sortBy).descending()
        else Sort.by(sortBy).ascending()

        val pageable = PageRequest.of(page, size, sort)
        val response = postServiceKt.getPosts(pageable)

        return ResponseEntity.ok(response)
    }

    // 게시글 상세조회
    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<PostDtoKt.PostDetailResponse> {
        val response = postServiceKt.getPost(id)
        return ResponseEntity.ok(response)
    }

    // 게시글 검색
    @GetMapping("/search")
    fun searchPost(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PostDtoKt.PostListResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val response = postServiceKt.searchPost(keyword, pageable)

        return ResponseEntity.ok(response)
    }


}