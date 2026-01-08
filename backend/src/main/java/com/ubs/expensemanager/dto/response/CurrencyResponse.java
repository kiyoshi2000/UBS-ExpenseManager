package com.ubs.expensemanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Response representing a Currency returned by the API.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyResponse {

    @Schema(description = "Currency identifier", example = "1")
    private Long id;

    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    private String name;

    @Schema(description = "Exchange rate relative to USD", example = "1.000000")
    private BigDecimal exchangeRate;
}
