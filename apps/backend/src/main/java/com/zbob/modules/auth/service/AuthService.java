package com.zbob.modules.auth.service;

import com.zbob.exception.ConflictException;
import com.zbob.exception.InvalidCredentialsException;
import com.zbob.modules.auth.dto.request.LoginRequest;
import com.zbob.modules.auth.dto.request.RegisterRequest;
import com.zbob.modules.auth.dto.response.AuthResponse;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import java.util.function.Predicate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    /** Compared against when the email is unknown, so both failure paths do the same hashing work. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("timing-equalizer");
    }

    @Transactional(readOnly = true)
    public AuthResponse loginCustomer(LoginRequest request) {
        return login(request, role -> role == Role.CUSTOMER);
    }

    @Transactional(readOnly = true)
    public AuthResponse loginBackOffice(LoginRequest request) {
        return login(request, Role::isBackOffice);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = users.save(new User(email, encoder.encode(request.password()), request.fullName().trim(), Role.CUSTOMER));
        return toResponse(user);
    }

    private AuthResponse login(LoginRequest request, Predicate<Role> allowed) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        boolean matches = encoder.matches(request.password(), user == null ? dummyHash : user.getPasswordHash());
        if (user == null || !matches || !user.isActive() || !allowed.test(user.getRole())) {
            throw new InvalidCredentialsException();
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                jwt.issue(user.getId(), user.getRole()),
                jwt.expirySeconds(),
                user.getRole().name(),
                user.getFullName());
    }
}
