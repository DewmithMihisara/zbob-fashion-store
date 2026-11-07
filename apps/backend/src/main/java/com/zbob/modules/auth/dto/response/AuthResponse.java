package com.zbob.modules.auth.dto.response;

public record AuthResponse(String accessToken, long expiresInSeconds, String role, String fullName) {
}
