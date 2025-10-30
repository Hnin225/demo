package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 사용자 목록 페이지
    @GetMapping("/list")
    public String listPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user/list";
    }

    // 사용자 등록 페이지
    @GetMapping("/write")
    public String writePage() {
        return "user/write";
    }

    // 사용자 수정 페이지
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "user/edit";
    }

    // REST API - 아이디 중복 확인
    @GetMapping("/api/check-userid")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUserId(@RequestParam String userId) {
        boolean exists = userService.checkUserIdDuplicate(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // REST API - 사용자 저장
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {
            // 신규 등록 시 아이디 중복 체크
            if (user.getId() == null) {
                if (userService.checkUserIdDuplicate(user.getUserId())) {
                    return ResponseEntity.badRequest().body("이미 사용 중인 아이디입니다.");
                }
            }

            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // REST API - 사용자 삭제
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // REST API - 사용자 검색
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<User>> searchUsers(@RequestParam(required = false) String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }
}