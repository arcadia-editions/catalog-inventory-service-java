package com.arcadiaeditions.catalog.inventory.core.inbound;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * Inbound Service Port for managing [StockReservation].
 */
@org.jspecify.annotations.NullMarked
public interface InventoryService {

    /**
     *
     *
     */
    public ReserveStockResult reserveStock(ReserveStockInput input);
    /**
     * The type of the triggering event sets the reason. No-op, no event, when already RELEASED.
     *
     * With Events: [StockReleased].
     *
     */
    public List<StockReservation> releaseStock(ReleaseStockInput input);
    /**
     * Reservation timer, 10 minutes after OrderCreated. No-op, no event, when no line of the order
     * is RESERVED. Releases them (RESERVATION_EXPIRED) only when payments answers NO_PAYMENT;
     * on STARTED, payment owns the end and nothing happens.
     *
     * With Events: [StockReleased].
     *
     */
    public List<StockReservation> expireReservation(ExpireReservationInput input);
    /**
     * Returns CONFIRMED with no change when every line is already CONFIRMED (redelivery).
     *
     * With Events: [StockReservationConfirmed].
     *
     */
    public ConfirmStockReservationResult confirmStockReservation(ConfirmStockReservationInput input);
}
