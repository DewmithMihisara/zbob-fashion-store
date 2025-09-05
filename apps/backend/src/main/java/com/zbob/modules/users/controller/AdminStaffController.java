package com.zbob.modules.users.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.users.dto.CreateStaffRequest;
import com.zbob.modules.users.dto.ResetPasswordRequest;
import com.zbob.modules.users.dto.UpdateStaffRequest;
import com.zbob.modules.users.dto.UserResponse;
import com.zbob.modules.users.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {

    private final StaffService staff;

    public AdminStaffController(StaffService staff) {
        this.staff = staff;
    }

    @GetMapping
    PageResponse<UserResponse> list(@PageableDefault(size = 50, sort = "fullName") Pageable pageable) {
        return staff.list(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse create(@Valid @RequestBody CreateStaffRequest request) {
        return staff.create(request);
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateStaffRequest request) {
        return staff.update(id, request);
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        staff.resetPassword(id, request.password());
    }
}
