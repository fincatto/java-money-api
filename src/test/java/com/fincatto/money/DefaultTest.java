package com.fincatto.money;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.convert.ExchangeRateType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryOperator;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTest {

    @Test
    @DisplayName("Converte codigo e locale em unidades de moeda")
    void moneyCurrency() {
        final CurrencyUnit currencyByCode = Monetary.getCurrency("USD");
        final CurrencyUnit currencyByLocale = Monetary.getCurrency(Locale.US);
        assertEquals(currencyByCode, currencyByLocale);
    }

    @Test
    @DisplayName("Testa padroes de moeda brasileira")
    void moneyBRLDefauls() {
        final CurrencyUnit currencyUnit = Monetary.getCurrency("BRL");
        assertEquals("BRL", currencyUnit.getCurrencyCode());
        assertEquals(2, currencyUnit.getDefaultFractionDigits());
    }

    @Test
    @DisplayName("Testa padroes de moeda americana")
    void moneyUSDDefauls() {
        final CurrencyUnit currencyUnit = Monetary.getCurrency("USD");
        assertEquals("USD", currencyUnit.getCurrencyCode());
        assertEquals(2, currencyUnit.getDefaultFractionDigits());
    }

    @Test
    @DisplayName("Testa padroes de moeda hungara")
    void moneyHUFDefauls() {
        final CurrencyUnit currencyUnit = Monetary.getCurrency("HUF");
        assertEquals("HUF", currencyUnit.getCurrencyCode());
        assertEquals(2, currencyUnit.getDefaultFractionDigits());
    }

    @Test
    @DisplayName("Arredondamento de moeda para real brasileiro")
    void moneyBRLRounding() {
        final MonetaryAmount valorDiversasCasas = Money.of(1.5006, "BRL");
        final MonetaryAmount valorArredondado = Monetary.getDefaultRounding().apply(valorDiversasCasas);
        final MonetaryAmount valorEsperado = Money.of(1.5, "BRL");
        assertEquals(valorEsperado, valorArredondado);
    }

    @Test
    @DisplayName("Conversao de dolar para real")
    @Disabled("So funciona no dia atual. Mantido para fins academicos.")
    void moneyConversionUSDToBRL() {
        final MonetaryAmount umDolar = Money.of(BigDecimal.ONE, "USD");

        final CurrencyConversion conversionDefault = MonetaryConversions.getConversion("BRL");
        final CurrencyConversion conversionIMF = MonetaryConversions.getConversion("BRL", ExchangeRateType.IMF.get());
        final CurrencyConversion conversionECB = MonetaryConversions.getConversion("BRL", ExchangeRateType.ECB.get());
        assertEquals(Money.of(3.895809, "BRL"), umDolar.with(conversionDefault));
        assertEquals(Money.of(3.895809, "BRL"), umDolar.with(conversionIMF));
        assertEquals(Money.of(3.900976, "BRL"), umDolar.with(conversionECB));
    }

    @Test
    @DisplayName("Taxa de conversao entre dois providers diferentes")
    @Disabled("Nunca que dois providers vao bater ne. Entao este teste eh soh para fins academicos.")
    void moneyExchangeRateProvided() {
        final ExchangeRateProvider provedorDeCambioIMF = MonetaryConversions.getExchangeRateProvider(ExchangeRateType.IMF);
        final ExchangeRate taxaCambioIMF = provedorDeCambioIMF.getExchangeRate("BRL", "USD");

        final ExchangeRateProvider provedorDeCambioPadrao = MonetaryConversions.getExchangeRateProvider(ExchangeRateType.ECB);
        final ExchangeRate taxaCambioPadrao = provedorDeCambioPadrao.getExchangeRate("BRL", "USD");

        assertEquals(taxaCambioIMF.getFactor().numberValue(BigDecimal.class), taxaCambioPadrao.getFactor().numberValue(BigDecimal.class));
    }

    @Test
    @DisplayName("Taxa de conversao para um exchange implementado")
    void moneyExchangeRateCustom() {
        final ExchangeRateProvider provedorDeCambio = new CustomRateProvider();
        final ExchangeRate taxaCambio = provedorDeCambio.getExchangeRate("BRL", "USD");
        assertEquals(BigDecimal.valueOf(1.5), taxaCambio.getFactor().numberValue(BigDecimal.class));
        assertEquals(BigDecimal.ONE, provedorDeCambio.getExchangeRate("BRL", "BRL").getFactor().numberValue(BigDecimal.class));
    }

    @Test
    @DisplayName("Formatacao de moedas em diferentes localidades")
    void moneyFormatLocales() {
        MonetaryAmountFormat germanyFormat = MonetaryFormats.getAmountFormat(Locale.GERMANY);
        assertEquals("1.123,50 EUR", germanyFormat.format(Money.of(1123.50, "EUR")));

        MonetaryAmountFormat italyFormat = MonetaryFormats.getAmountFormat(Locale.ITALY);
        assertEquals("1.123,50 EUR", italyFormat.format(Money.of(1123.50, "EUR")));

        MonetaryAmountFormat brazilFormat = MonetaryFormats.getAmountFormat(new Locale("pt", "BR"));
        assertEquals("BRL 1.123,50", brazilFormat.format(Money.of(1123.50, "BRL")));

        MonetaryAmountFormat usFormat = MonetaryFormats.getAmountFormat(Locale.US);
        assertEquals("USD1,123.50", usFormat.format(Money.of(1123.50, "USD")));
    }

    @Test
    @DisplayName("Operacao monetaria simples em cima de valores em reais")
    void moneyMonetayOperationSimple() {
        final CurrencyUnit real = Monetary.getCurrency("BRL");
        final MonetaryAmount cem = Money.of(100, real);
        final MonetaryAmount cinquenta = Money.of(50, real);

        assertEquals(Money.of(150, real), cem.add(cinquenta));
        assertEquals(Money.of(50, real), cem.subtract(cinquenta));
        assertEquals(Money.of(200, real), cem.multiply(2));
        assertEquals(Money.of(50, real), cem.divide(2));

        assertTrue(cem.isEqualTo(cem));
        assertFalse(cem.isEqualTo(cinquenta));

        assertTrue(cem.isGreaterThan(cinquenta));
        assertTrue(cem.isGreaterThanOrEqualTo(cem));
        assertTrue(cem.isGreaterThanOrEqualTo(cinquenta));
        assertFalse(cinquenta.isGreaterThanOrEqualTo(cem));

        assertFalse(cem.isLessThan(cinquenta));
        assertTrue(cinquenta.isLessThan(cem));

        assertFalse(cem.isLessThanOrEqualTo(cinquenta));
        assertTrue(cem.isLessThanOrEqualTo(cem));

        assertFalse(cem.isNegative());
        assertFalse(cem.isNegativeOrZero());
    }

    @Test
    @DisplayName("Operacao monetaria complexa em cima de valores em reais")
    void moneyMonetayOperationComplex() {
        final MonetaryOperator dezPorCento = (MonetaryAmount amount) -> {
            BigDecimal baseAmount = amount.getNumber().numberValue(BigDecimal.class);
            BigDecimal tenPercent = baseAmount.multiply(new BigDecimal("0.1"));
            return Money.of(tenPercent, amount.getCurrency());
        };

        final Money milPilas = Money.of(1000, "BRL");
        final Money cemPilas = Money.of(100, "BRL");
        assertEquals(cemPilas, milPilas.with(dezPorCento));
    }
}