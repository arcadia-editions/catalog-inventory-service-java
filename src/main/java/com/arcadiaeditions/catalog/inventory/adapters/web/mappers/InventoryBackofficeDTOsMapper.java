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
public interface InventoryBackofficeDTOsMapper {

    InventoryBackofficeDTOsMapper INSTANCE = Mappers.getMapper(InventoryBackofficeDTOsMapper.class);

    // request mappings
    AdjustStockInput asAdjustStockInput(AdjustStockInputDTO dto);

    CreateInventoryPositionInput asCreateInventoryPositionInput(CreateInventoryPositionInputDTO dto);

    ReceiveStockInput asReceiveStockInput(ReceiveStockInputDTO dto);

    // response mappings

    InventoryPositionDTO asInventoryPositionDTO(InventoryPosition entity);
}
