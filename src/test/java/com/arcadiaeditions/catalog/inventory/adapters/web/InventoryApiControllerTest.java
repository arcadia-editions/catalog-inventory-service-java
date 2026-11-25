package com.arcadiaeditions.catalog.inventory.adapters.web;

import com.arcadiaeditions.catalog.inventory.adapters.web.dtos.*;
import com.arcadiaeditions.catalog.inventory.config.ServicesInMemoryConfig;
import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test controller for InventoryApiController.
 */
public class InventoryApiControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    ServicesInMemoryConfig context = new ServicesInMemoryConfig();

    InventoryApiController controller = new InventoryApiController(context.inventoryService());

    @BeforeEach
    void setUp() {
        context.reloadTestData();
    }

    @Test
    public void reserveStockTest() {
        ReserveStockInputDTO reqBody = null;
        var response = controller.reserveStock(reqBody);
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void confirmStockReservationTest() {
        ConfirmStockReservationInputDTO reqBody = null;
        var response = controller.confirmStockReservation(reqBody);
        Assertions.assertEquals(201, response.getStatusCode().value());
    }
}
