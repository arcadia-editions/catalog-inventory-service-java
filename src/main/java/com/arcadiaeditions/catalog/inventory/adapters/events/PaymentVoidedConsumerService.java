package com.arcadiaeditions.catalog.inventory.adapters.events;

import com.arcadiaeditions.catalog.inventory.asyncapi.client.IPaymentVoidedEventV1ConsumerService;
import com.arcadiaeditions.catalog.inventory.core.inbound.InventoryService;
import com.arcadiaeditions.payments.processing.events.avro.PaymentVoided;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: payment-voided-event-v1 -> InventoryService.releaseStock.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class PaymentVoidedConsumerService implements IPaymentVoidedEventV1ConsumerService {

    private final InventoryService inventoryService;

    public PaymentVoidedConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void onPaymentVoidedEventV1(PaymentVoided payload, PaymentVoidedHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to ReleaseStockInput and call inventoryService.releaseStock
        throw new UnsupportedOperationException("Not implemented");
    }
}
