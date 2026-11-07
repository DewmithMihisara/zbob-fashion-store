package com.zbob.modules.activity;

import com.zbob.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Records every change made through the admin API, and every export, once the request has finished. */
@Component
public class ActivityInterceptor implements HandlerInterceptor {

    private final ActivityService activity;

    public ActivityInterceptor(ActivityService activity) {
        this.activity = activity;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path = request.getRequestURI();
        boolean export = path.startsWith("/api/v1/admin/exports/");
        if (!export && "GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        int status = ex != null && response.getStatus() < 400 ? 500 : response.getStatus();
        if (status >= 400) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long actorId = auth != null && auth.getPrincipal() instanceof AuthenticatedUser user ? user.id() : null;
        String fallback = auth != null ? auth.getName() : "unknown";
        activity.record(actorId, fallback, ActivityDescriber.action(request.getMethod(), path),
                ActivityDescriber.summary(request.getMethod(), path), status);
    }
}
