package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.response.CurrencyResponse;
import com.ubs.expensemanager.mapper.CurrencyMapper;
import com.ubs.expensemanager.repository.CurrencyRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final CurrencyMapper currencyMapper;

    /**
     * Retrieves all available currencies.
     *
     * @return list of currencies with their exchange rates
     */
    public List<CurrencyResponse> listAll() {
        return currencyRepository.findAll()
                .stream()
                .map(currencyMapper::toResponse)
                .collect(Collectors.toList());
    }

}
