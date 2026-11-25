package com.arcadiaeditions.catalog.inventory.core.inbound.dtos;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * ConfirmStockReservationResult.
 */
public class ConfirmStockReservationResult implements Serializable {

    @NotNull
    private ConfirmStockReservationOutcome outcome;

    private StockReservationConfirmedResult confirmed;

    private StockReservationNotHeldResult notHeld;

    public ConfirmStockReservationOutcome getOutcome() {
        return outcome;
    }

    public ConfirmStockReservationResult setOutcome(ConfirmStockReservationOutcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public StockReservationConfirmedResult getConfirmed() {
        return confirmed;
    }

    public ConfirmStockReservationResult setConfirmed(StockReservationConfirmedResult confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public StockReservationNotHeldResult getNotHeld() {
        return notHeld;
    }

    public ConfirmStockReservationResult setNotHeld(StockReservationNotHeldResult notHeld) {
        this.notHeld = notHeld;
        return this;
    }
}
