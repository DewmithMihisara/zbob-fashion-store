package com.zbob.modules.returns;

public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    REJECTED,
    COMPLETED;

    public boolean canMoveTo(ReturnStatus next) {
        return switch (this) {
            case REQUESTED -> next == APPROVED || next == REJECTED;
            case APPROVED -> next == COMPLETED || next == REJECTED;
            case REJECTED, COMPLETED -> false;
        };
    }

    public boolean isOpen() {
        return this == REQUESTED || this == APPROVED;
    }
}
