package com.zbob.modules.users.entity;

public enum Role {
    ADMIN,
    STAFF,
    CUSTOMER;

    public boolean isBackOffice() {
        return this == ADMIN || this == STAFF;
    }
}
