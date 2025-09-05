package com.zbob.modules.users.mapper;

import com.zbob.modules.users.dto.UserResponse;
import com.zbob.modules.users.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.isActive(), u.getCreatedAt());
    }
}
