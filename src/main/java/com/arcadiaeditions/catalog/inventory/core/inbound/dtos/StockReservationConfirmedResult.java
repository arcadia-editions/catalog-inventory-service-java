package com.arcadiaeditions.catalog.inventory.core.inbound.dtos;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * StockReservationConfirmedResult.
 */
public class StockReservationConfirmedResult implements Serializable {

    private String orderId;

    private Instant confirmedAt;

    public String getOrderId() {
        return orderId;
    }

    public StockReservationConfirmedResult setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public StockReservationConfirmedResult setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
        return this;
    }
}
