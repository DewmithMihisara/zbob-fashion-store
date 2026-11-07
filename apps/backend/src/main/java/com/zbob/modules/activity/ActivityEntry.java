package com.zbob.modules.activity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** One thing someone did in the dashboard. The actor's name is copied, so entries survive the account being deleted. */
@Entity
@Table(name = "activity_log")
public class ActivityEntry extends BaseEntity {

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    protected ActivityEntry() {
    }

    public ActivityEntry(Long actorId, String actorName, String action, String summary, int statusCode) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.action = action;
        this.summary = summary.length() > 300 ? summary.substring(0, 300) : summary;
        this.statusCode = statusCode;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getAction() {
        return action;
    }

    public String getSummary() {
        return summary;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
