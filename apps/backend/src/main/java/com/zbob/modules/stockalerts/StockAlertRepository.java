package com.zbob.modules.stockalerts;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    boolean existsByVariantIdAndEmailAndNotifiedAtIsNull(Long variantId, String email);

    /** Waiting alerts for rows that have stock again. Reads the stock from the database, after pending changes are saved. */
    @Query("""
            select a from StockAlert a join fetch a.variant v join fetch v.product
            where a.notifiedAt is null and v.id in :variantIds and v.stockQuantity > 0""")
    List<StockAlert> findReadyToSend(Collection<Long> variantIds);

    long countByVariantIdAndNotifiedAtIsNull(Long variantId);
}
