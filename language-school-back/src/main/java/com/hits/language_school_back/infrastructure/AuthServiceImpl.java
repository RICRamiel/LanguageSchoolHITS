package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.TokenDTO;
import com.hits.language_school_back.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public TokenDTO register(RegisterDTO request) {
        return null;
    }

    @Override
    public TokenDTO login(String email, String password) {
        return null;
    }

    @Override
    public void logout(HttpServletRequest request) {

    }
}
