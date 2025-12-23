package com.zbob.modules.auth.service;

import com.zbob.common.ratelimit.RateLimits;
import com.zbob.exception.ConflictException;
import com.zbob.exception.InvalidCredentialsException;
import com.zbob.modules.auth.dto.request.LoginRequest;
import com.zbob.modules.auth.dto.request.RegisterRequest;
import com.zbob.modules.auth.dto.response.AuthResponse;
import com.zbob.modules.notifications.EmailService;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import java.util.Locale;
import java.util.function.Predicate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final RateLimits limits;
    private final EmailService emails;
    /** Compared against when the email is unknown, so both failure paths do the same hashing work. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, RateLimits limits, EmailService emails) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.limits = limits;
        this.emails = emails;
        this.dummyHash = encoder.encode("timing-equalizer");
    }

    @Transactional(readOnly = true)
    public AuthResponse loginCustomer(LoginRequest request, String ip) {
        return login(request, ip, role -> role == Role.CUSTOMER);
    }

    @Transactional(readOnly = true)
    public AuthResponse loginBackOffice(LoginRequest request, String ip) {
        return login(request, ip, Role::isBackOffice);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ip) {
        RateLimits.enforce(limits.signupPerIp().tryAcquire("signup:" + ip));
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = users.save(new User(email, encoder.encode(request.password()), request.fullName().trim(), Role.CUSTOMER));
        emails.welcome(user);
        return toResponse(user);
    }

    /**
     * Failures count against both the email and the address they came from. A locked email stays locked
     * even for the right password, so guessing gains nothing; a success clears that email's count.
     */
    private AuthResponse login(LoginRequest request, String ip, Predicate<Role> allowed) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String emailKey = "login:" + email;
        String ipKey = "login-ip:" + ip;
        RateLimits.enforce(Math.max(
                limits.loginPerEmail().secondsUntilAllowed(emailKey),
                limits.loginPerIp().secondsUntilAllowed(ipKey)));

        User user = users.findByEmailIgnoreCase(email).orElse(null);
        boolean matches = encoder.matches(request.password(), user == null ? dummyHash : user.getPasswordHash());
        if (user == null || !matches || !user.isActive() || !allowed.test(user.getRole())) {
            limits.loginPerEmail().record(emailKey);
            limits.loginPerIp().record(ipKey);
            throw new InvalidCredentialsException();
        }
        limits.loginPerEmail().clear(emailKey);
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
