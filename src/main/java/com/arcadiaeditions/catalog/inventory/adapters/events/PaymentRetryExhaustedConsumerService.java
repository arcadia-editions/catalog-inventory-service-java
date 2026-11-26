package com.arcadiaeditions.catalog.inventory.adapters.events;

import com.arcadiaeditions.catalog.inventory.asyncapi.client.IPaymentRetryExhaustedEventV1ConsumerService;
import com.arcadiaeditions.catalog.inventory.core.inbound.InventoryService;
import com.arcadiaeditions.payments.processing.events.avro.PaymentRetryExhausted;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: payment-retry-exhausted-event-v1 -> InventoryService.releaseStock.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class PaymentRetryExhaustedConsumerService implements IPaymentRetryExhaustedEventV1ConsumerService {

    private final InventoryService inventoryService;

    public PaymentRetryExhaustedConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void onPaymentRetryExhaustedEventV1(PaymentRetryExhausted payload, PaymentRetryExhaustedHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to ReleaseStockInput and call inventoryService.releaseStock
        throw new UnsupportedOperationException("Not implemented");
    }
}
