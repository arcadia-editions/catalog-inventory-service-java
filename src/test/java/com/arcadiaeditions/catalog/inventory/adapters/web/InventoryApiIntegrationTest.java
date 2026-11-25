package com.arcadiaeditions.catalog.inventory.adapters.web;

import static org.springframework.http.HttpMethod.*;

import com.arcadiaeditions.catalog.inventory.adapters.web.dtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Integration tests for the {@link InventoryApi} REST controller.
 */
public class InventoryApiIntegrationTest extends BaseWebTestClientTest {

    /**
     * Test: reserveStock for OK.
     */
    @Test
    public void testReserveStock_201() {
        String requestBody = """
            {
              "orderId" : "orderId-sfqy1mxb32dsgwr1u8",
              "items" : [ {
                "sku" : "sku-jw7ozybzsxa4a8syz1",
                "quantity" : 92
              } ]
            }
        """;

        webTestClient
                .method(POST)
                .uri("/api/inventory/reservations/reserve")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(201)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.outcome")
                .isNotEmpty()
                .jsonPath("$.reserved")
                .isNotEmpty()
                .jsonPath("$.reserved.orderId")
                .isNotEmpty()
                .jsonPath("$.reserved.expiresAt")
                .isNotEmpty()
                .jsonPath("$.reserved.reservations")
                .isNotEmpty()
                .jsonPath("$.unavailable")
                .isNotEmpty()
                .jsonPath("$.unavailable.orderId")
                .isNotEmpty()
                .jsonPath("$.unavailable.unavailableItems")
                .isNotEmpty();
    }

    /**
     * Test: reserveStock for Invalid request.
     */
    @Test
    public void testReserveStock_400() {
        String requestBody = """
            {
              "orderId" : "orderId-7ck79bqpdwxuveqykl",
              "items" : [ {
                "sku" : "sku-kc38rm",
                "quantity" : 41
              } ]
            }
        """;

        webTestClient
                .method(POST)
                .uri("/api/inventory/reservations/reserve")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(400);
    }

    /**
     * Test: Returns CONFIRMED with no change when every line is already CONFIRMED (redelivery). for OK.
     */
    @Test
    public void testConfirmStockReservation_201() {
        String requestBody = """
            {
              "orderId" : "orderId-r0p12jsx1jhf40sq00"
            }
        """;

        webTestClient
                .method(POST)
                .uri("/api/inventory/reservations/confirm")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(201)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.outcome")
                .isNotEmpty()
                .jsonPath("$.confirmed")
                .isNotEmpty()
                .jsonPath("$.confirmed.orderId")
                .isNotEmpty()
                .jsonPath("$.confirmed.confirmedAt")
                .isNotEmpty()
                .jsonPath("$.notHeld")
                .isNotEmpty()
                .jsonPath("$.notHeld.orderId")
                .isNotEmpty();
    }

    /**
     * Test: Returns CONFIRMED with no change when every line is already CONFIRMED (redelivery). for Invalid request.
     */
    @Test
    public void testConfirmStockReservation_400() {
        String requestBody = """
            {
              "orderId" : "orderId-90ntfvsi3"
            }
        """;

        webTestClient
                .method(POST)
                .uri("/api/inventory/reservations/confirm")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(400);
    }
}
