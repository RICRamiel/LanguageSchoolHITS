package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.service.UserService;
import com.hits.language_school_back.model.Group;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User grantDeanRole(Long userId) {
        return null;
    }

    @Override
    public User loadUserByUsername(String username) {
        return null;
    }

    @Override
    public User grantRole(Long userId, Role role) {
        return null;
    }

    @Override
    public List<UserDTO> getUsers(Group group) {
        return List.of();
    }

    @Override
    public UserFullDTO getMe(HttpServletRequest request) {
        return null;
    }
}
