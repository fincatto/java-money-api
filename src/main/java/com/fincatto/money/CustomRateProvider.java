package com.fincatto.money;

import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.Monetary;
import javax.money.convert.*;
import java.math.BigDecimal;

public class CustomRateProvider extends AbstractRateProvider {

    private static final ProviderContext CONTEXT = ProviderContextBuilder.of("DFP", RateType.OTHER).set("providerDescription", "Custom Provider").build();

    public CustomRateProvider() {
        super(CONTEXT);
    }

    @Override
    public boolean isAvailable(ConversionQuery conversionQuery) {
        return conversionQuery.getBaseCurrency().getCurrencyCode().equals(conversionQuery.getCurrency().getCurrencyCode());
    }

    @Override
    public ExchangeRate getExchangeRate(String baseCode, String termCode) {
        final ExchangeRateBuilder builder = new ExchangeRateBuilder(getContext().getProviderName(), RateType.OTHER).setBase(Monetary.getCurrency(baseCode));
        builder.setFactor(baseCode.equalsIgnoreCase(termCode) ? DefaultNumberValue.of(BigDecimal.ONE) : DefaultNumberValue.of(new BigDecimal("1.5")));
        builder.setTerm(Monetary.getCurrency(termCode));
        return builder.build();
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        if (conversionQuery.getBaseCurrency().getCurrencyCode().equals(conversionQuery.getCurrency().getCurrencyCode())) {
            final ExchangeRateBuilder builder = new ExchangeRateBuilder(getContext().getProviderName(), RateType.OTHER).setBase(conversionQuery.getBaseCurrency());
            builder.setFactor(DefaultNumberValue.of(BigDecimal.ONE));
            builder.setTerm(conversionQuery.getCurrency());
            return builder.build();
        }
        return null;
    }

    @Override
    public ExchangeRate getReversed(ExchangeRate rate) {
        if (rate.getContext().getProviderName().equals(CONTEXT.getProviderName())) {
            return new ExchangeRateBuilder(rate.getContext()).setTerm(rate.getBaseCurrency()).setBase(rate.getCurrency()).setFactor(new DefaultNumberValue(BigDecimal.ONE)).build();
        }
        return null;
    }
}
