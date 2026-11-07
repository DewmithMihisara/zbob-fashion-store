package com.zbob.modules.auth.service;

import com.zbob.common.ratelimit.RateLimits;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.auth.entity.PasswordResetToken;
import com.zbob.modules.auth.repository.PasswordResetTokenRepository;
import com.zbob.modules.notifications.EmailService;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    static final Duration LINK_LIFETIME = Duration.ofHours(1);
    private static final String INVALID = "This reset link is invalid or has expired. Please ask for a new one.";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService emails;
    private final RateLimits limits;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, PasswordEncoder encoder,
            EmailService emails, RateLimits limits) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.emails = emails;
        this.limits = limits;
    }

    /**
     * Emails a reset link when the address belongs to an active customer. The caller always gets the same
     * answer, so this can not be used to find out who has an account.
     */
    @Transactional
    public void request(String rawEmail, String ip) {
        RateLimits.enforce(limits.resetPerIp().tryAcquire("reset-ip:" + ip));
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (limits.resetPerEmail().tryAcquire("reset:" + email) > 0) {
            return; // quietly, for the same reason
        }
        users.findByEmailIgnoreCase(email)
                .filter(user -> user.getRole() == Role.CUSTOMER && user.isActive())
                .ifPresent(this::issue);
    }

    /** Lets staff send a link to a customer who is locked out. */
    @Transactional
    public void sendToCustomer(Long customerId) {
        User user = users.findById(customerId)
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        if (!user.isActive()) {
            throw new ConflictException("This account is disabled. Enable it before sending a reset link.");
        }
        issue(user);
    }

    @Transactional
    public void confirm(String token, String newPassword) {
        Instant now = Instant.now();
        PasswordResetToken reset = tokens.findByTokenHash(hash(token))
                .filter(t -> t.isUsableAt(now))
                .orElseThrow(() -> new BadRequestException(INVALID));
        User user = users.findById(reset.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new BadRequestException(INVALID));
        user.setPasswordHash(encoder.encode(newPassword));
        tokens.invalidateForUser(user.getId(), now);
        emails.passwordChanged(user);
    }

    private void issue(User user) {
        Instant now = Instant.now();
        tokens.invalidateForUser(user.getId(), now);
        String token = newToken();
        tokens.save(new PasswordResetToken(user.getId(), hash(token), now.plus(LINK_LIFETIME)));
        emails.passwordReset(user, token);
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }
}
