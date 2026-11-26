package com.arcadiaeditions.catalog.inventory.adapters.events;

import com.arcadiaeditions.catalog.inventory.asyncapi.client.IPaymentDeclinedEventV1ConsumerService;
import com.arcadiaeditions.catalog.inventory.core.inbound.InventoryService;
import com.arcadiaeditions.payments.processing.events.avro.PaymentDeclined;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: payment-declined-event-v1 -> InventoryService.releaseStock.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class PaymentDeclinedConsumerService implements IPaymentDeclinedEventV1ConsumerService {

    private final InventoryService inventoryService;

    public PaymentDeclinedConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void onPaymentDeclinedEventV1(PaymentDeclined payload, PaymentDeclinedHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to ReleaseStockInput and call inventoryService.releaseStock
        throw new UnsupportedOperationException("Not implemented");
    }
}
