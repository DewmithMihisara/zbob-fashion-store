package com.zbob.modules.returns;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReturnStatusTest {

    @Test
    void aRequestIsApprovedOrRejected() {
        assertThat(ReturnStatus.REQUESTED.canMoveTo(ReturnStatus.APPROVED)).isTrue();
        assertThat(ReturnStatus.REQUESTED.canMoveTo(ReturnStatus.REJECTED)).isTrue();
        assertThat(ReturnStatus.REQUESTED.canMoveTo(ReturnStatus.COMPLETED)).isFalse();
    }

    @Test
    void anApprovedReturnIsCompletedWhenTheItemsArrive() {
        assertThat(ReturnStatus.APPROVED.canMoveTo(ReturnStatus.COMPLETED)).isTrue();
    }

    @Test
    void finishedReturnsDoNotChange() {
        for (ReturnStatus next : ReturnStatus.values()) {
            assertThat(ReturnStatus.REJECTED.canMoveTo(next)).isFalse();
            assertThat(ReturnStatus.COMPLETED.canMoveTo(next)).isFalse();
        }
    }

    @Test
    void openMeansStillBeingHandled() {
        assertThat(ReturnStatus.REQUESTED.isOpen()).isTrue();
        assertThat(ReturnStatus.APPROVED.isOpen()).isTrue();
        assertThat(ReturnStatus.REJECTED.isOpen()).isFalse();
    }
}
