package com.hits.language_school_back.inteface;

import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.TokenDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    TokenDTO register(RegisterDTO request);
    TokenDTO login(String email, String password);
    void logout(HttpServletRequest request);
}
