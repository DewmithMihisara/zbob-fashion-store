package com.zbob.modules.users.dto;

import com.zbob.modules.users.entity.Role;
import java.time.Instant;

public record UserResponse(Long id, String email, String fullName, Role role, boolean active, Instant createdAt) {
}
