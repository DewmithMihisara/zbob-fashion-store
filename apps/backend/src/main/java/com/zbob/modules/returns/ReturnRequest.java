package com.zbob.modules.returns;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "return_requests")
public class ReturnRequest extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Column(name = "staff_note", length = 1000)
    private String staffNote;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected ReturnRequest() {
    }

    public ReturnRequest(Long orderId, Long userId, String reason) {
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
    }

    public void decide(ReturnStatus next, String note, Instant at) {
        this.status = next;
        this.staffNote = note;
        this.decidedAt = at;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getReason() {
        return reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public String getStaffNote() {
        return staffNote;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
