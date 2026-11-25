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
 * REST controller for InventoryBackofficeApi.
 */
@RestController
@RequestMapping("/api")
public class InventoryBackofficeApiController implements InventoryBackofficeApi {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private NativeWebRequest request;

    private InventoryBackofficeService inventoryBackofficeService;

    @Autowired
    public InventoryBackofficeApiController setInventoryBackofficeService(
            InventoryBackofficeService inventoryBackofficeService) {
        this.inventoryBackofficeService = inventoryBackofficeService;
        return this;
    }

    private InventoryBackofficeDTOsMapper mapper = InventoryBackofficeDTOsMapper.INSTANCE;

    public InventoryBackofficeApiController(InventoryBackofficeService inventoryBackofficeService) {

        this.inventoryBackofficeService = inventoryBackofficeService;
    }

    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<InventoryPositionDTO> createInventoryPosition(CreateInventoryPositionInputDTO reqBody) {
        log.debug("REST request to createInventoryPosition: {}", reqBody);
        var input = mapper.asCreateInventoryPositionInput(reqBody);
        var inventoryPosition = inventoryBackofficeService.createInventoryPosition(input);
        InventoryPositionDTO responseDTO = mapper.asInventoryPositionDTO(inventoryPosition);
        return ResponseEntity.status(201).body(responseDTO);
    }

    @Override
    public ResponseEntity<InventoryPositionDTO> receiveStock(String sku, ReceiveStockInputDTO reqBody) {
        log.debug("REST request to receiveStock: {}, {}", sku, reqBody);
        var input = mapper.asReceiveStockInput(reqBody);
        var inventoryPosition = inventoryBackofficeService.receiveStock(input);
        InventoryPositionDTO responseDTO = mapper.asInventoryPositionDTO(inventoryPosition);
        return ResponseEntity.status(201).body(responseDTO);
    }

    @Override
    public ResponseEntity<InventoryPositionDTO> adjustStock(String sku, AdjustStockInputDTO reqBody) {
        log.debug("REST request to adjustStock: {}, {}", sku, reqBody);
        var input = mapper.asAdjustStockInput(reqBody);
        var inventoryPosition = inventoryBackofficeService.adjustStock(input);
        InventoryPositionDTO responseDTO = mapper.asInventoryPositionDTO(inventoryPosition);
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
