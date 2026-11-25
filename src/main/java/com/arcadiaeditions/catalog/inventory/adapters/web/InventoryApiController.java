package com.arcadiaeditions.catalog.inventory.adapters.web;

import com.arcadiaeditions.catalog.inventory.adapters.web.dtos.*;
import com.arcadiaeditions.catalog.inventory.adapters.web.mappers.*;
import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * REST controller for InventoryApi.
 */
@RestController
@RequestMapping("/api")
public class InventoryApiController implements InventoryApi {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private NativeWebRequest request;

    private InventoryService inventoryService;

    @Autowired
    public InventoryApiController setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        return this;
    }

    private InventoryDTOsMapper mapper = InventoryDTOsMapper.INSTANCE;

    public InventoryApiController(InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }

    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<ReserveStockResultDTO> reserveStock(ReserveStockInputDTO reqBody) {
        log.debug("REST request to reserveStock: {}", reqBody);
        var input = mapper.asReserveStockInput(reqBody);
        var reserveStockResult = inventoryService.reserveStock(input);
        ReserveStockResultDTO responseDTO = mapper.asReserveStockResultDTO(reserveStockResult);
        return ResponseEntity.status(201).body(responseDTO);
    }

    @Override
    public ResponseEntity<ConfirmStockReservationResultDTO> confirmStockReservation(
            ConfirmStockReservationInputDTO reqBody) {
        log.debug("REST request to confirmStockReservation: {}", reqBody);
        var input = mapper.asConfirmStockReservationInput(reqBody);
        var confirmStockReservationResult = inventoryService.confirmStockReservation(input);
        ConfirmStockReservationResultDTO responseDTO =
                mapper.asConfirmStockReservationResultDTO(confirmStockReservationResult);
        return ResponseEntity.status(201).body(responseDTO);
    }

    protected Pageable pageOf(Integer page, Integer limit, List<String> sort) {
        Sort sortOrder = sort != null
                ? Sort.by(sort.stream()
                        .map(sortParam -> {
                            String[] parts = sortParam.split(":");
                            String property = parts[0];
                            Sort.Direction direction =
                                    parts.length > 1 ? Sort.Direction.fromString(parts[1]) : Sort.Direction.ASC;
                            return new Sort.Order(direction, property);
                        })
                        .toList())
                : Sort.unsorted();
        return PageRequest.of(page != null ? page : 0, limit != null ? limit : 10, sortOrder);
    }
}
