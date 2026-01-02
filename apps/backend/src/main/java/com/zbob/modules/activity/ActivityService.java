package com.zbob.modules.activity;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.users.repository.UserRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final ActivityRepository entries;
    private final UserRepository users;

    public ActivityService(ActivityRepository entries, UserRepository users) {
        this.entries = entries;
        this.users = users;
    }

    /** Saved in its own transaction, and a failure here never breaks the action being recorded. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorId, String fallbackName, String action, String summary, int status) {
        try {
            String name = actorId == null ? fallbackName : users.findById(actorId).map(u -> u.getFullName() + " <" + u.getEmail() + ">").orElse(fallbackName);
            entries.save(new ActivityEntry(actorId, name, action, summary, status));
        } catch (RuntimeException e) {
            log.warn("Could not record activity: {}", summary, e);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityResponse> list(Pageable pageable) {
        return PageResponse.of(entries.findAll(pageable), e -> new ActivityResponse(e.getId(), e.getActorName(), e.getAction(),
                e.getSummary(), e.getStatusCode(), e.getCreatedAt()));
    }

    public record ActivityResponse(Long id, String actor, String action, String summary, int status, Instant at) {
    }
}
