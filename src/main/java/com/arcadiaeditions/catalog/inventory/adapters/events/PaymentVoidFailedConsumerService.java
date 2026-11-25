package com.arcadiaeditions.catalog.inventory.adapters.events;

import com.arcadiaeditions.catalog.inventory.asyncapi.client.IPaymentVoidFailedEventV1ConsumerService;
import com.arcadiaeditions.catalog.inventory.core.inbound.InventoryService;
import com.arcadiaeditions.payments.processing.events.avro.PaymentVoidFailed;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: payment-void-failed-event-v1 -> InventoryService.releaseStock.
 * Stub so the Spring context boots; the generator does not emit adapters for AsyncAPIGenerator interfaces.
 */
@Component
public class PaymentVoidFailedConsumerService implements IPaymentVoidFailedEventV1ConsumerService {

    private final InventoryService inventoryService;

    public PaymentVoidFailedConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void onPaymentVoidFailedEventV1(PaymentVoidFailed payload, PaymentVoidFailedHeaders headers) {
        // TODO CUSTOM_REQUIRED: map payload to ReleaseStockInput and call inventoryService.releaseStock
        throw new UnsupportedOperationException("Not implemented");
    }
}
