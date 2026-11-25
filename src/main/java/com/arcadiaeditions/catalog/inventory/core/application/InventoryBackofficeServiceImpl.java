package com.arcadiaeditions.catalog.inventory.core.application;

import com.arcadiaeditions.catalog.inventory.asyncapi.provider.*;
import com.arcadiaeditions.catalog.inventory.core.application.mappers.*;
import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import com.arcadiaeditions.catalog.inventory.core.outbound.jpa.*;
// import com.arcadiaeditions.catalog.inventory.events.avro.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing [InventoryPosition].
 */
@Service
@Transactional
@org.jspecify.annotations.NullMarked
public class InventoryBackofficeServiceImpl implements InventoryBackofficeService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final InventoryBackofficeServiceMapper inventoryBackofficeServiceMapper =
            InventoryBackofficeServiceMapper.INSTANCE;

    private final InventoryPositionRepository inventoryPositionRepository;

    private final EventsMapper eventsMapper = EventsMapper.INSTANCE;
    private final InventoryBackofficeEventsProducer eventsProducer;

    /**
     * Constructor.
     */
    public InventoryBackofficeServiceImpl(
            InventoryPositionRepository inventoryPositionRepository, InventoryBackofficeEventsProducer eventsProducer) {
        this.inventoryPositionRepository = inventoryPositionRepository;
        this.eventsProducer = eventsProducer;
    }

    public InventoryPosition createInventoryPosition(CreateInventoryPositionInput input) {

        log.debug("[CRUD] Request to save InventoryPosition: {}", input);
        var inventoryPosition = inventoryBackofficeServiceMapper.update(new InventoryPosition(), input);
        inventoryPosition = inventoryPositionRepository.save(inventoryPosition);
        // TODO: may need to reload the entity to fetch relationships 'mapped by id'
        return inventoryPosition;
    }

    public InventoryPosition receiveStock(ReceiveStockInput input) {

        log.debug("Request receiveStock: {}", input);

        var inventoryPosition = inventoryBackofficeServiceMapper.update(new InventoryPosition(), input);
        // TODO: implement this method
        // emit events
        var stockReceived = eventsMapper.asStockReceived(inventoryPosition);
        eventsProducer.onStockReceived(stockReceived);
        return inventoryPosition;
    }

    public InventoryPosition adjustStock(AdjustStockInput input) {

        log.debug("Request adjustStock: {}", input);

        var inventoryPosition = inventoryBackofficeServiceMapper.update(new InventoryPosition(), input);
        // TODO: implement this method
        // emit events
        var stockAdjusted = eventsMapper.asStockAdjusted(inventoryPosition);
        eventsProducer.onStockAdjusted(stockAdjusted);
        return inventoryPosition;
    }
}
