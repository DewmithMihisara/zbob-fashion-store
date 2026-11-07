package com.zbob.security;

import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Authenticates requests carrying a valid bearer token; others continue anonymously. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIX)) {
            // The role is read from the database, so demoting or disabling someone applies immediately.
            jwtService.verify(header.substring(PREFIX.length()))
                    .flatMap(token -> users.findById(token.id()))
                    .filter(User::isActive)
                    .ifPresent(user -> {
                        var principal = new AuthenticatedUser(user.getId(), user.getRole());
                        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                        var context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of(authority)));
                        SecurityContextHolder.setContext(context);
                    });
        }
        chain.doFilter(request, response);
    }
}
