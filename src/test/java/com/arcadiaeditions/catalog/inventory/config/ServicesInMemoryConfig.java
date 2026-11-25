package com.arcadiaeditions.catalog.inventory.config;

import com.arcadiaeditions.catalog.inventory.asyncapi.provider.*;
import com.arcadiaeditions.catalog.inventory.core.application.*;
import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.*;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Services InMemory Config. It can be used standalone or with @SpringBootTest.
 */
@Configuration
@Profile("in-memory")
public class ServicesInMemoryConfig extends RepositoriesInMemoryConfig {

    protected final EventsProducerInMemoryContext eventsProducerInMemoryContext = new EventsProducerInMemoryContext();

    protected final InventoryServiceImpl inventoryService = new InventoryServiceImpl(
            stockReservationRepository(), eventsProducerInMemoryContext.inventoryEventsProducer());
    protected final InventoryBackofficeServiceImpl inventoryBackofficeService = new InventoryBackofficeServiceImpl(
            inventoryPositionRepository(), eventsProducerInMemoryContext.inventoryBackofficeEventsProducer());

    @Bean
    public <T extends InventoryService> T inventoryService() {
        return (T) inventoryService;
    }

    @Bean
    public <T extends InventoryBackofficeService> T inventoryBackofficeService() {
        return (T) inventoryBackofficeService;
    }

    static List<StockReservation> _stockReservations;
    static List<InventoryPosition> _inventoryPositions;

    public void reloadTestData() {

        var testDataLoader = new TestDataLoader(List.of(StockReservation.class, InventoryPosition.class));
        var stockReservations = _stockReservations != null
                ? _stockReservations
                : testDataLoader.loadCollectionTestDataAsObjects(StockReservation.class);
        stockReservationRepository().deleteAll();
        stockReservationRepository().saveAll(stockReservations);
        var inventoryPositions = _inventoryPositions != null
                ? _inventoryPositions
                : testDataLoader.loadCollectionTestDataAsObjects(InventoryPosition.class);
        inventoryPositionRepository().deleteAll();
        inventoryPositionRepository().saveAll(inventoryPositions);
    }

    public EventsProducerInMemoryContext getEventsProducerInMemoryContext() {
        return eventsProducerInMemoryContext;
    }
}
