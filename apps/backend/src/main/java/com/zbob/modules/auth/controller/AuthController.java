package com.zbob.modules.auth.controller;

import com.zbob.modules.auth.dto.request.LoginRequest;
import com.zbob.modules.auth.dto.request.RegisterRequest;
import com.zbob.modules.auth.dto.response.AuthResponse;
import com.zbob.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/customer/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return auth.register(request, http.getRemoteAddr());
    }

    @PostMapping("/customer/login")
    AuthResponse customerLogin(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return auth.loginCustomer(request, http.getRemoteAddr());
    }

    @PostMapping("/admin/login")
    AuthResponse adminLogin(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return auth.loginBackOffice(request, http.getRemoteAddr());
    }
}
