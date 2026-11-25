package com.arcadiaeditions.catalog.inventory.core.application;

import static org.mockito.Mockito.*;

import com.arcadiaeditions.catalog.inventory.config.*;
import com.arcadiaeditions.catalog.inventory.core.application.mappers.*;
import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import com.arcadiaeditions.catalog.inventory.core.outbound.jpa.*;
import com.arcadiaeditions.catalog.inventory.infrastructure.jpa.inmemory.*;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acceptance Test for InventoryService.
 */
class InventoryServiceTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    ServicesInMemoryConfig context = new ServicesInMemoryConfig();
    InventoryServiceImpl inventoryService = context.inventoryService();

    StockReservationRepositoryInMemory stockReservationRepository = context.stockReservationRepository();

    @BeforeEach
    void setUp() {
        context.reloadTestData();
    }

    @Test
    void reserveStockTest() { // TODO: implement this test
    }

    @Test
    void releaseStockTest() { // TODO: implement this test
    }

    @Test
    void expireReservationTest() { // TODO: implement this test
    }

    @Test
    void confirmStockReservationTest() { // TODO: implement this test
    }
}
