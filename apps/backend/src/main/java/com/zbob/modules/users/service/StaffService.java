package com.zbob.modules.users.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.users.dto.CreateStaffRequest;
import com.zbob.modules.users.dto.UpdateStaffRequest;
import com.zbob.modules.users.dto.UserResponse;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.mapper.UserMapper;
import com.zbob.modules.users.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private static final List<Role> BACK_OFFICE = List.of(Role.ADMIN, Role.STAFF);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public StaffService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.of(users.findByRoleIn(BACK_OFFICE, pageable), UserMapper::toResponse);
    }

    @Transactional
    public UserResponse create(CreateStaffRequest request) {
        requireBackOfficeRole(request.role());
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User(email, encoder.encode(request.password()), request.fullName().trim(), request.role());
        return UserMapper.toResponse(users.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateStaffRequest request) {
        requireBackOfficeRole(request.role());
        User user = find(id);
        boolean removesAdmin = user.getRole() == Role.ADMIN && user.isActive()
                && (request.role() != Role.ADMIN || !request.active());
        if (removesAdmin && users.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw new ConflictException("The store must keep at least one active admin");
        }
        user.setFullName(request.fullName().trim());
        user.setRole(request.role());
        user.setActive(request.active());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public void resetPassword(Long id, String password) {
        find(id).setPasswordHash(encoder.encode(password));
    }

    private User find(Long id) {
        return users.findById(id)
                .filter(u -> u.getRole().isBackOffice())
                .orElseThrow(() -> new ResourceNotFoundException("Staff member", id));
    }

    private static void requireBackOfficeRole(Role role) {
        if (!role.isBackOffice()) {
            throw new BadRequestException("Staff accounts must be ADMIN or STAFF");
        }
    }
}
