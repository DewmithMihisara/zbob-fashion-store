package com.zbob.modules.users.dto;

import com.zbob.modules.users.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStaffRequest(@NotBlank @Size(max = 120) String fullName, @NotNull Role role, boolean active) {
}
