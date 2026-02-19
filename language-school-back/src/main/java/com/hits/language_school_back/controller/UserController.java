package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.service.UserService;
import com.hits.language_school_back.model.Group;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(required = false) Group group
    ) {
        List<UserDTO> users = userService.getUsers(group);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/grant-dean-role")
    public ResponseEntity<User> grantDeanRole(@PathVariable Long userId) {
        User updatedUser = userService.grantDeanRole(userId);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/grant-role")
    public ResponseEntity<User> grantRole(@PathVariable Long userId, @RequestParam Role role) {
        User updatedUser = userService.grantRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserFullDTO> me(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getMe(request));
    }
}