package com.arcadiaeditions.catalog.inventory.core.inbound.dtos;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * ConfirmStockReservationInput.
 */
public class ConfirmStockReservationInput implements Serializable {

    @NotNull
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public ConfirmStockReservationInput setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }
}
