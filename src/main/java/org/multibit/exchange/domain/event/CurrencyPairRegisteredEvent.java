package org.multibit.exchange.domain.event;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.multibit.exchange.domain.model.CurrencyPair;
import org.multibit.exchange.infrastructure.adaptor.eventapi.CurrencyPairId;
import org.multibit.exchange.infrastructure.adaptor.eventapi.ExchangeId;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Event used to indicate that a {@link CurrencyPair} was registered with an Exchange.</p>
 *
 * @since 0.0.1
 *  
 */
public class CurrencyPairRegisteredEvent {

  @TargetAggregateIdentifier
  private final ExchangeId exchangeId;
  private final CurrencyPairId currencyPairId;
  private final String baseCurrency;
  private final String counterCurrency;

  public CurrencyPairRegisteredEvent(ExchangeId exchangeId, CurrencyPairId currencyPairId, String baseCurrency, String counterCurrency) {
    checkNotNull(exchangeId, "exchangeId must not be null");
    checkNotNull(currencyPairId, "currencyPairId must not be null");
    checkNotNull(baseCurrency, "baseCurrency must not be null");
    checkNotNull(counterCurrency, "counterCurrency must not be null");

    this.exchangeId = exchangeId;
    this.currencyPairId = currencyPairId;
    this.baseCurrency = baseCurrency;
    this.counterCurrency = counterCurrency;
  }

  public ExchangeId getExchangeId() {
    return exchangeId;
  }

  public CurrencyPairId getCurrencyPairId() {
    return currencyPairId;
  }

  public String getBaseCurrency() {
    return baseCurrency;
  }

  public String getCounterCurrency() {
    return counterCurrency;
  }
}
