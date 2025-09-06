package com.zbob.modules.promotions;

import static org.assertj.core.api.Assertions.assertThat;

import com.zbob.modules.promotions.entity.DiscountType;
import com.zbob.modules.promotions.entity.Promotion;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PromotionTest {

    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    @Test
    void percentDiscountRoundsDown() {
        assertThat(new Promotion("A", DiscountType.PERCENT, 15).discountOn(999)).isEqualTo(149);
    }

    @Test
    void fixedDiscountNeverExceedsTheSubtotal() {
        assertThat(new Promotion("A", DiscountType.FIXED, 5000).discountOn(3000)).isEqualTo(3000);
    }

    @Test
    void largeSubtotalsDoNotOverflow() {
        assertThat(new Promotion("A", DiscountType.PERCENT, 50).discountOn(100_000_000)).isEqualTo(50_000_000);
    }

    @Test
    void minimumSpendIsEnforced() {
        Promotion promotion = new Promotion("A", DiscountType.FIXED, 500);
        promotion.setMinimumCents(2000);

        assertThat(promotion.isUsableAt(NOW, 1999)).isFalse();
        assertThat(promotion.isUsableAt(NOW, 2000)).isTrue();
    }

    @Test
    void dateWindowIsEnforced() {
        Promotion promotion = new Promotion("A", DiscountType.FIXED, 500);
        promotion.setStartsAt(NOW);
        promotion.setEndsAt(NOW.plus(Duration.ofDays(1)));

        assertThat(promotion.isUsableAt(NOW.minusSeconds(1), 100)).isFalse();
        assertThat(promotion.isUsableAt(NOW, 100)).isTrue();
        assertThat(promotion.isUsableAt(NOW.plus(Duration.ofDays(1)), 100)).isFalse();
    }

    @Test
    void inactivePromotionIsNotUsable() {
        Promotion promotion = new Promotion("A", DiscountType.FIXED, 500);
        promotion.setActive(false);

        assertThat(promotion.isUsableAt(NOW, 100)).isFalse();
    }
}
