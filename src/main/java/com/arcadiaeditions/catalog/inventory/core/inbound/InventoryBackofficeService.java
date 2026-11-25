package com.arcadiaeditions.catalog.inventory.core.inbound;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * Inbound Service Port for managing [InventoryPosition].
 */
@org.jspecify.annotations.NullMarked
public interface InventoryBackofficeService {

    /**
     *
     *
     */
    public InventoryPosition createInventoryPosition(CreateInventoryPositionInput input);
    /**
     *
     *
     * With Events: [StockReceived].
     *
     */
    public InventoryPosition receiveStock(ReceiveStockInput input);
    /**
     *
     *
     * With Events: [StockAdjusted].
     *
     */
    public InventoryPosition adjustStock(AdjustStockInput input);
}
