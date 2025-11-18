package com.example.openfeign.user;

import com.example.openfeign.common.User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, User> userDatabase = new HashMap<>();

    public UserController() {
        // 초기 데이터
        userDatabase.put(1L, new User(1L, "김철수", "kim@example.com", "010-1234-5678"));
        userDatabase.put(2L, new User(2L, "이영희", "lee@example.com", "010-2345-6789"));
        userDatabase.put(3L, new User(3L, "박민수", "park@example.com", "010-3456-7890"));
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        User user = userDatabase.get(id);
        if (user == null) {
            throw new RuntimeException("User not found: " + id);
        }
        return user;
    }

    @GetMapping
    public Map<Long, User> getAllUsers() {
        return userDatabase;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        userDatabase.put(user.getId(), user);
        return user;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userDatabase.containsKey(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        user.setId(id);
        userDatabase.put(id, user);
        return user;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userDatabase.remove(id);
    }
}
