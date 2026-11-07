package com.zbob.modules.activity;

import com.zbob.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/activity")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityController {

    private final ActivityService activity;

    public AdminActivityController(ActivityService activity) {
        this.activity = activity;
    }

    @GetMapping
    PageResponse<ActivityService.ActivityResponse> list(
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return activity.list(pageable);
    }
}
