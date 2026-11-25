package com.arcadiaeditions.catalog.inventory.adapters.web.mappers;

import com.arcadiaeditions.catalog.inventory.adapters.web.dtos.*;
import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = BaseMapper.class)
public interface InventoryDTOsMapper {

    InventoryDTOsMapper INSTANCE = Mappers.getMapper(InventoryDTOsMapper.class);

    // request mappings
    ConfirmStockReservationInput asConfirmStockReservationInput(ConfirmStockReservationInputDTO dto);

    ReserveStockInput asReserveStockInput(ReserveStockInputDTO dto);

    // response mappings

    ConfirmStockReservationResultDTO asConfirmStockReservationResultDTO(ConfirmStockReservationResult entity);

    ReserveStockResultDTO asReserveStockResultDTO(ReserveStockResult entity);
}
