package com.arcadiaeditions.catalog.inventory.core.application.mappers;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {BaseMapper.class})
public interface EventsMapper {

    EventsMapper INSTANCE = Mappers.getMapper(EventsMapper.class);

    com.arcadiaeditions.catalog.inventory.events.avro.StockAdjusted asStockAdjusted(
            InventoryPosition inventoryPosition);

    com.arcadiaeditions.catalog.inventory.events.avro.StockReservationConfirmed asStockReservationConfirmed(
            ConfirmStockReservationInput input);

    com.arcadiaeditions.catalog.inventory.events.avro.StockReleased asStockReleased(StockReservation stockReservation);

    com.arcadiaeditions.catalog.inventory.events.avro.StockReleaseReason asStockReleaseReason(
            StockReleaseReason stockReleaseReason);

    com.arcadiaeditions.catalog.inventory.events.avro.StockAdjustmentReason asStockAdjustmentReason(
            StockAdjustmentReason stockAdjustmentReason);

    com.arcadiaeditions.catalog.inventory.events.avro.StockReceived asStockReceived(
            InventoryPosition inventoryPosition);
}
