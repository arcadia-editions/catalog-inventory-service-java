package com.arcadiaeditions.catalog.inventory.infrastructure.jpa.inmemory;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.outbound.jpa.StockReservationRepository;
import java.math.*;
import java.time.*;
import java.util.*;

public class StockReservationRepositoryInMemory extends InMemoryJpaRepository<StockReservation>
        implements StockReservationRepository {}
