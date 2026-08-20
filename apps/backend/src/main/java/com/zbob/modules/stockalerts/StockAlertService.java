package com.zbob.modules.stockalerts;

import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.notifications.EmailService;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockAlertService {

    private final StockAlertRepository alerts;
    private final ProductVariantRepository variants;
    private final EmailService emails;

    public StockAlertService(StockAlertRepository alerts, ProductVariantRepository variants, EmailService emails) {
        this.alerts = alerts;
        this.variants = variants;
        this.emails = emails;
    }

    /** Asking twice for the same thing keeps one request, so the shopper gets one email. */
    @Transactional
    public void subscribe(Long variantId, String rawEmail) {
        ProductVariant variant = variants.findById(variantId)
                .filter(v -> v.getProduct().isPublished())
                .orElseThrow(() -> new ResourceNotFoundException("Item", variantId));
        if (variant.getStockQuantity() > 0) {
            throw new ConflictException("This is in stock now, so you can order it straight away");
        }
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!alerts.existsByVariantIdAndEmailAndNotifiedAtIsNull(variantId, email)) {
            alerts.save(new StockAlert(variant, email));
        }
    }

    /** Called after any stock change. Emails everyone waiting for rows that now have stock, once each. */
    @Transactional
    public void notifyIfInStock(Collection<Long> variantIds) {
        if (variantIds.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        for (StockAlert alert : alerts.findReadyToSend(variantIds)) {
            ProductVariant v = alert.getVariant();
            emails.backInStock(alert.getEmail(), v.getProduct().getName(), v.getSize(), v.getColor(), v.getProduct().getSlug());
            alert.markNotified(now);
        }
    }
}
