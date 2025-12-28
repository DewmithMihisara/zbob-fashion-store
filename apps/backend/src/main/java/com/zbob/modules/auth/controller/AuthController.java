package com.zbob.modules.auth.controller;

import com.zbob.modules.auth.dto.request.LoginRequest;
import com.zbob.modules.auth.dto.request.PasswordResetConfirmRequest;
import com.zbob.modules.auth.dto.request.PasswordResetRequest;
import com.zbob.modules.auth.dto.request.RegisterRequest;
import com.zbob.modules.auth.dto.response.AuthResponse;
import com.zbob.modules.auth.service.AuthService;
import com.zbob.modules.auth.service.PasswordResetService;
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
    private final PasswordResetService passwordReset;

    public AuthController(AuthService auth, PasswordResetService passwordReset) {
        this.auth = auth;
        this.passwordReset = passwordReset;
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

    @PostMapping("/customer/password-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void requestPasswordReset(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest http) {
        passwordReset.request(request.email(), http.getRemoteAddr());
    }

    @PostMapping("/customer/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordReset.confirm(request.token(), request.newPassword());
    }

    @PostMapping("/admin/login")
    AuthResponse adminLogin(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return auth.loginBackOffice(request, http.getRemoteAddr());
    }
}
