package com.example.elkmonitoring.controller;

import com.example.elkmonitoring.domain.User;
import com.example.elkmonitoring.dto.UserRequest;
import com.example.elkmonitoring.dto.UserResponse;
import com.example.elkmonitoring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 API 컨트롤러
 * REST API 엔드포인트 정의 및 요청/응답 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 모든 사용자 조회
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/users - 전체 사용자 조회");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자 조회
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - 사용자 조회", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 생성
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        log.info("POST /api/users - 신규 사용자 생성");
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * 사용자 수정
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        log.info("PUT /api/users/{} - 사용자 수정", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 삭제
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - 사용자 삭제", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 사용자 상태 변경
     * PATCH /api/users/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> changeUserStatus(
            @PathVariable Long id,
            @RequestParam User.UserStatus status) {
        log.info("PATCH /api/users/{}/status - 사용자 상태 변경: {}", id, status);
        UserResponse user = userService.changeUserStatus(id, status);
        return ResponseEntity.ok(user);
    }

    /**
     * 에러 시뮬레이션 (테스트용)
     * GET /api/users/test/error
     */
    @GetMapping("/test/error")
    public ResponseEntity<Void> simulateError() {
        log.info("GET /api/users/test/error - 에러 시뮬레이션");
        userService.simulateError();
        return ResponseEntity.ok().build();
    }
}
