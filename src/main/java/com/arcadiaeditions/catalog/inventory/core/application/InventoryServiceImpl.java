package com.arcadiaeditions.catalog.inventory.core.application;

import com.arcadiaeditions.catalog.inventory.asyncapi.provider.*;
import com.arcadiaeditions.catalog.inventory.core.application.mappers.*;
import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.*;
import com.arcadiaeditions.catalog.inventory.core.inbound.dtos.*;
import com.arcadiaeditions.catalog.inventory.core.outbound.jpa.*;
// import com.arcadiaeditions.catalog.inventory.events.avro.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing [StockReservation].
 */
@Service
@Transactional
@org.jspecify.annotations.NullMarked
public class InventoryServiceImpl implements InventoryService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final InventoryServiceMapper inventoryServiceMapper = InventoryServiceMapper.INSTANCE;

    private final StockReservationRepository stockReservationRepository;

    private final EventsMapper eventsMapper = EventsMapper.INSTANCE;
    private final InventoryEventsProducer eventsProducer;

    /**
     * Constructor.
     */
    public InventoryServiceImpl(
            StockReservationRepository stockReservationRepository, InventoryEventsProducer eventsProducer) {
        this.stockReservationRepository = stockReservationRepository;
        this.eventsProducer = eventsProducer;
    }

    public ReserveStockResult reserveStock(ReserveStockInput input) {

        log.debug("Request reserveStock: {}", input);

        var stockReservation = inventoryServiceMapper.update(new StockReservation(), input);
        // TODO: implement this method
        return inventoryServiceMapper.asReserveStockResult(stockReservation);
    }

    public List<StockReservation> releaseStock(ReleaseStockInput input) {

        log.debug("Request releaseStock: {}", input);

        // TODO CUSTOM_REQUIRED: load the StockReservation(s) this transition applies to, e.g. by a correlation key in
        // the input
        List<StockReservation> stockReservations = List.of();
        stockReservations.forEach(existingStockReservation -> {
            StockReservationTransitions.ensureCanReleaseStock(existingStockReservation);
            inventoryServiceMapper.update(existingStockReservation, input);
            existingStockReservation.setStatus(ReservationStatus.RELEASED);
        });
        stockReservations = stockReservationRepository.saveAll(stockReservations);
        // emit events
        stockReservations.forEach(eventSource -> {
            var stockReleased = eventsMapper.asStockReleased(eventSource);
            eventsProducer.onStockReleased(stockReleased);
        });
        return stockReservations;
    }

    public List<StockReservation> expireReservation(ExpireReservationInput input) {
        // TODO: call PaymentsProcessingZdl PaymentsProcessingService.getPaymentStatus(GetPaymentStatusInput) ->
        // GetPaymentStatusResult

        log.debug("Request expireReservation: {}", input);

        // TODO CUSTOM_REQUIRED: load the StockReservation(s) this transition applies to, e.g. by a correlation key in
        // the input
        List<StockReservation> stockReservations = List.of();
        stockReservations.forEach(existingStockReservation -> {
            StockReservationTransitions.ensureCanExpireReservation(existingStockReservation);
            inventoryServiceMapper.update(existingStockReservation, input);
            existingStockReservation.setStatus(ReservationStatus.RELEASED);
        });
        stockReservations = stockReservationRepository.saveAll(stockReservations);
        // emit events
        stockReservations.forEach(eventSource -> {
            var stockReleased = eventsMapper.asStockReleased(eventSource);
            eventsProducer.onStockReleased(stockReleased);
        });
        return stockReservations;
    }

    public ConfirmStockReservationResult confirmStockReservation(ConfirmStockReservationInput input) {

        log.debug("Request confirmStockReservation: {}", input);

        // TODO CUSTOM_REQUIRED: load the StockReservation this transition applies to, e.g. by a correlation key in the
        // input
        Optional<StockReservation> foundStockReservation = Optional.empty();
        var stockReservation = foundStockReservation
                .map(existingStockReservation -> {
                    StockReservationTransitions.ensureCanConfirmStockReservation(existingStockReservation);
                    inventoryServiceMapper.update(existingStockReservation, input);
                    existingStockReservation.setStatus(ReservationStatus.CONFIRMED);
                    return existingStockReservation;
                })
                .map(stockReservationRepository::save)
                .orElseThrow();
        // emit events
        var stockReservationConfirmed = eventsMapper.asStockReservationConfirmed(input);
        eventsProducer.onStockReservationConfirmed(stockReservationConfirmed);
        return inventoryServiceMapper.asConfirmStockReservationResult(stockReservation);
    }
}
