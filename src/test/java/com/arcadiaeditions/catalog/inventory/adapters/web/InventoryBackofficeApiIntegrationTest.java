package com.arcadiaeditions.catalog.inventory.adapters.web;

import static org.springframework.http.HttpMethod.*;

import com.arcadiaeditions.catalog.inventory.adapters.web.dtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Integration tests for the {@link InventoryBackofficeApi} REST controller.
 */
public class InventoryBackofficeApiIntegrationTest extends BaseWebTestClientTest {

    /**
     * Test: createInventoryPosition for OK.
     */
    @Test
    public void testCreateInventoryPosition_201() {
        String requestBody = """
            {
              "sku" : "sku-3yzxkqj3i",
              "initialQuantity" : 74
            }
        """;

        webTestClient
                .method(POST)
                .uri("/api/inventory/positions")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(201)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isNotEmpty()
                .jsonPath("$.version")
                .isNotEmpty()
                .jsonPath("$.sku")
                .isNotEmpty()
                .jsonPath("$.availableQuantity")
                .isNotEmpty()
                .jsonPath("$.reservedQuantity")
                .isNotEmpty()
                .jsonPath("$.soldQuantity")
                .isNotEmpty();
    }

    /**
     * Test: createInventoryPosition for Invalid request.
     */
    @Test
    public void testCreateInventoryPosition_400() {
        String requestBody = """
            {
              "sku" : "sku-lm",
              "initialQuantity" : 83
            }
        """;

        webTestClient
                .method(POST)
                .uri("/api/inventory/positions")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(400);
    }

    /**
     * Test: receiveStock for OK.
     */
    @Test
    public void testReceiveStock_201() {
        String requestBody = """
            {
              "sku" : "sku-8a7002xqr1",
              "quantity" : 53,
              "reason" : "reason-b2d303eng7",
              "reference" : "reference-as1"
            }
        """;
        var sku = "";

        webTestClient
                .method(POST)
                .uri("/api/inventory/{sku}/stock-intakes", sku)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(201)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isNotEmpty()
                .jsonPath("$.version")
                .isNotEmpty()
                .jsonPath("$.sku")
                .isNotEmpty()
                .jsonPath("$.availableQuantity")
                .isNotEmpty()
                .jsonPath("$.reservedQuantity")
                .isNotEmpty()
                .jsonPath("$.soldQuantity")
                .isNotEmpty();
    }

    /**
     * Test: receiveStock for Invalid request.
     */
    @Test
    public void testReceiveStock_400() {
        String requestBody = """
            {
              "sku" : "sku-h",
              "quantity" : 29,
              "reason" : "reason-52zqb3h20n34euge97c",
              "reference" : "reference-vhyjl0xokd"
            }
        """;
        var sku = "";

        webTestClient
                .method(POST)
                .uri("/api/inventory/{sku}/stock-intakes", sku)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(400);
    }

    /**
     * Test: adjustStock for OK.
     */
    @Test
    public void testAdjustStock_201() {
        String requestBody = """
            {
              "sku" : "sku-qmmw1zz1h0641u0r",
              "quantityDelta" : 42,
              "reason" : "DAMAGED",
              "reference" : "reference-a56orv8z2"
            }
        """;
        var sku = "";

        webTestClient
                .method(POST)
                .uri("/api/inventory/{sku}/adjustments", sku)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(201)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isNotEmpty()
                .jsonPath("$.version")
                .isNotEmpty()
                .jsonPath("$.sku")
                .isNotEmpty()
                .jsonPath("$.availableQuantity")
                .isNotEmpty()
                .jsonPath("$.reservedQuantity")
                .isNotEmpty()
                .jsonPath("$.soldQuantity")
                .isNotEmpty();
    }

    /**
     * Test: adjustStock for Invalid request.
     */
    @Test
    public void testAdjustStock_400() {
        String requestBody = """
            {
              "sku" : "sku-yuq2m13vqikei45alb9qkf",
              "quantityDelta" : 80,
              "reason" : "MANUAL_CORRECTION",
              "reference" : "reference-9svt"
            }
        """;
        var sku = "";

        webTestClient
                .method(POST)
                .uri("/api/inventory/{sku}/adjustments", sku)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(400);
    }
}
