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
 * Test controller for InventoryBackofficeApiController.
 */
public class InventoryBackofficeApiControllerTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    ServicesInMemoryConfig context = new ServicesInMemoryConfig();

    InventoryBackofficeApiController controller =
            new InventoryBackofficeApiController(context.inventoryBackofficeService());

    @BeforeEach
    void setUp() {
        context.reloadTestData();
    }

    @Test
    public void createInventoryPositionTest() {
        CreateInventoryPositionInputDTO reqBody = null;
        var response = controller.createInventoryPosition(reqBody);
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void receiveStockTest() {
        String sku = null;
        ReceiveStockInputDTO reqBody = null;
        var response = controller.receiveStock(sku, reqBody);
        Assertions.assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void adjustStockTest() {
        String sku = null;
        AdjustStockInputDTO reqBody = null;
        var response = controller.adjustStock(sku, reqBody);
        Assertions.assertEquals(201, response.getStatusCode().value());
    }
}
