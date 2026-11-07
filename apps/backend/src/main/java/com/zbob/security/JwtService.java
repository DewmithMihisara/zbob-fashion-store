package com.zbob.security;

import com.zbob.config.AppProperties;
import com.zbob.modules.users.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Issues and verifies the signed tokens used by the storefront and the admin dashboard. */
@Service
public class JwtService {

    private final SecretKey key;
    private final AppProperties.Jwt config;
    private final Clock clock = Clock.systemUTC();

    public JwtService(AppProperties properties) {
        this.config = properties.jwt();
        // hmacShaKeyFor rejects secrets under 256 bits, so a weak secret stops startup.
        this.key = Keys.hmacShaKeyFor(config.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issue(Long userId, Role role) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(config.expiry())))
                .signWith(key)
                .compact();
    }

    public Optional<AuthenticatedUser> verify(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()), Role.valueOf(claims.get("role", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long expirySeconds() {
        return config.expiry().toSeconds();
    }
}
