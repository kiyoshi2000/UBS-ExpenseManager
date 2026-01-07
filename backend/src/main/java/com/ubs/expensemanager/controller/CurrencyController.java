package com.ubs.expensemanager.controller;

import com.ubs.expensemanager.dto.response.CurrencyResponse;
import com.ubs.expensemanager.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for managing currencies.
 *
 * <p>Provides endpoints to retrieve available currencies and their exchange rates.</p>
 */
@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Tag(name = "Currencies", description = "Currency management endpoints")
public class CurrencyController {

    private final CurrencyService currencyService;

    /**
     * Lists all available currencies with their exchange rates.
     *
     * @return list of currencies
     */
    @GetMapping
    @Operation(summary = "List all currencies", description = "Retrieves all available currencies with their exchange rates")
    public ResponseEntity<List<CurrencyResponse>> listAll() {
        return ResponseEntity.ok(currencyService.listAll());
    }
}
