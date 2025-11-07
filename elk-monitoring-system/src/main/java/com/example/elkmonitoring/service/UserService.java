package com.example.elkmonitoring.service;

import com.example.elkmonitoring.config.LoggingUtils;
import com.example.elkmonitoring.domain.User;
import com.example.elkmonitoring.dto.UserRequest;
import com.example.elkmonitoring.dto.UserResponse;
import com.example.elkmonitoring.exception.BusinessException;
import com.example.elkmonitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 사용자 서비스
 * 비즈니스 로직 처리 및 구조화된 로깅 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 모든 사용자 조회
     */
    public List<UserResponse> getAllUsers() {
        long startTime = System.currentTimeMillis();

        log.info("전체 사용자 조회 시작");
        List<User> users = userRepository.findAll();

        long duration = System.currentTimeMillis() - startTime;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_count", users.size());

        LoggingUtils.logPerformance(log, "전체_사용자_조회", duration, metadata);

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ID로 사용자 조회
     */
    public UserResponse getUserById(Long id) {
        log.debug("사용자 조회 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - ID: {}", id);
                    return new BusinessException(
                            "사용자를 찾을 수 없습니다: " + id,
                            "USER_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        log.info("사용자 조회 성공 - 이메일: {}", user.getEmail());
        return UserResponse.from(user);
    }

    /**
     * 사용자 생성
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.info("신규 사용자 생성 시작 - 이메일: {}", request.getEmail());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("이메일 중복 - 이메일: {}", request.getEmail());
            throw new BusinessException(
                    "이미 존재하는 이메일입니다: " + request.getEmail(),
                    "DUPLICATE_EMAIL",
                    HttpStatus.CONFLICT
            );
        }

        User user = request.toEntity();
        User savedUser = userRepository.save(user);

        // 비즈니스 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", savedUser.getId());
        eventData.put("user_email", savedUser.getEmail());
        eventData.put("user_name", savedUser.getName());
        eventData.put("user_status", savedUser.getStatus().name());

        LoggingUtils.logBusinessEvent(log, "사용자_생성", eventData);

        log.info("사용자 생성 완료 - ID: {}", savedUser.getId());
        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 수정
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("사용자 수정 시작 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "사용자를 찾을 수 없습니다: " + id,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        // 이메일 변경 시 중복 체크
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            log.warn("이메일 중복 - 이메일: {}", request.getEmail());
            throw new BusinessException(
                    "이미 존재하는 이메일입니다: " + request.getEmail(),
                    "DUPLICATE_EMAIL",
                    HttpStatus.CONFLICT
            );
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);

        // 비즈니스 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", updatedUser.getId());
        eventData.put("user_email", updatedUser.getEmail());

        LoggingUtils.logBusinessEvent(log, "사용자_수정", eventData);

        log.info("사용자 수정 완료 - ID: {}", updatedUser.getId());
        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("사용자 삭제 시작 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "사용자를 찾을 수 없습니다: " + id,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        userRepository.delete(user);

        // 비즈니스 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", id);
        eventData.put("user_email", user.getEmail());

        LoggingUtils.logBusinessEvent(log, "사용자_삭제", eventData);

        log.info("사용자 삭제 완료 - ID: {}", id);
    }

    /**
     * 사용자 상태 변경
     */
    @Transactional
    public UserResponse changeUserStatus(Long id, User.UserStatus newStatus) {
        log.info("사용자 상태 변경 시작 - ID: {}, 새 상태: {}", id, newStatus);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "사용자를 찾을 수 없습니다: " + id,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        User.UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);

        User updatedUser = userRepository.save(user);

        // 상태 변경 이벤트 로깅
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("user_id", id);
        eventData.put("old_status", oldStatus.name());
        eventData.put("new_status", newStatus.name());

        LoggingUtils.logBusinessEvent(log, "사용자_상태변경", eventData);

        log.info("사용자 상태 변경 완료 - ID: {}", id);
        return UserResponse.from(updatedUser);
    }

    /**
     * 의도적인 에러 발생 (테스트용)
     */
    public void simulateError() {
        log.warn("테스트용 에러 시뮬레이션 시작");

        try {
            // 의도적으로 예외 발생
            throw new RuntimeException("ELK 테스트를 위한 시뮬레이션 에러입니다");
        } catch (Exception e) {
            Map<String, Object> errorContext = new HashMap<>();
            errorContext.put("error_type", "시뮬레이션");
            errorContext.put("test_purpose", true);

            LoggingUtils.logError(log, "시뮬레이션 에러 발생", e, errorContext);
            throw e;
        }
    }
}
