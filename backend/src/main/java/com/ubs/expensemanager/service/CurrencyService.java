package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.CurrencyResponse;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for handling business logic related to Currencies.
 *
 * <p>This class manages currency information including exchange rates.
 * Currently uses hardcoded values, but designed for future integration
 * with external exchange rate APIs.</p>
 */
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    /**
     * Retrieves all available currencies.
     *
     * @return list of currencies with their exchange rates
     */
    public List<CurrencyResponse> listAll() {
        return currencyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Currency entity to a CurrencyResponse DTO.
     */
    private CurrencyResponse toResponse(Currency currency) {
        return CurrencyResponse.builder()
                .id(currency.getId())
                .name(currency.getName())
                .exchangeRate(currency.getExchangeRate())
                .build();
    }
}
