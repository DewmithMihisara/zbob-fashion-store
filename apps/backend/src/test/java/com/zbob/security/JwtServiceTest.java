package com.zbob.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zbob.config.AppProperties;
import com.zbob.modules.users.entity.Role;
import io.jsonwebtoken.security.WeakKeyException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-unit-test";

    private static JwtService service(String secret, Duration expiry) {
        return new JwtService(new AppProperties(new AppProperties.Jwt(secret, expiry), null, null, null));
    }

    @Test
    void roundTripsUserIdAndRole() {
        JwtService jwt = service(SECRET, Duration.ofHours(1));

        assertThat(jwt.verify(jwt.issue(42L, Role.STAFF)))
                .contains(new AuthenticatedUser(42L, Role.STAFF));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService jwt = service(SECRET, Duration.ofSeconds(-1));

        assertThat(jwt.verify(jwt.issue(1L, Role.ADMIN))).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String foreign = service(SECRET + "-other", Duration.ofHours(1)).issue(1L, Role.ADMIN);

        assertThat(service(SECRET, Duration.ofHours(1)).verify(foreign)).isEmpty();
    }

    @Test
    void rejectsTamperedAndGarbageTokens() {
        JwtService jwt = service(SECRET, Duration.ofHours(1));
        String token = jwt.issue(1L, Role.CUSTOMER);

        assertThat(jwt.verify(token.substring(0, token.length() - 3) + "abc")).isEmpty();
        assertThat(jwt.verify("not-a-token")).isEmpty();
    }

    @Test
    void refusesWeakSecret() {
        assertThatThrownBy(() -> service("short", Duration.ofHours(1))).isInstanceOf(WeakKeyException.class);
    }
}
