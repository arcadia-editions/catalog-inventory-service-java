package com.arcadiaeditions.catalog.inventory.infrastructure.jpa.inmemory;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import com.arcadiaeditions.catalog.inventory.core.outbound.jpa.InventoryPositionRepository;
import java.math.*;
import java.time.*;
import java.util.*;

public class InventoryPositionRepositoryInMemory extends InMemoryJpaRepository<InventoryPosition>
        implements InventoryPositionRepository {}
