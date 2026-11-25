package com.arcadiaeditions.catalog.inventory.core.outbound.jpa;

import com.arcadiaeditions.catalog.inventory.core.domain.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InventoryPosition entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InventoryPositionRepository extends JpaRepository<InventoryPosition, Long> {}
