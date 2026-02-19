package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.Group;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public interface UserService {
    User grantDeanRole(Long userId);
    User loadUserByUsername(String username);
    User grantRole(Long userId, Role role);
    List<UserDTO> getUsers(Group group);
    UserFullDTO getMe(HttpServletRequest request);
}
