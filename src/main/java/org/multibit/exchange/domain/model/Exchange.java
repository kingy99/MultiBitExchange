package org.multibit.exchange.domain.model;

import com.google.common.collect.Maps;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.eventsourcing.annotation.EventSourcedMember;
import org.multibit.exchange.domain.event.CurrencyPairRegisteredEvent;
import org.multibit.exchange.domain.event.CurrencyPairRemovedEvent;
import org.multibit.exchange.domain.event.ExchangeCreatedEvent;
import org.multibit.exchange.infrastructure.adaptor.eventapi.CreateExchangeCommand;
import org.multibit.exchange.infrastructure.adaptor.eventapi.CurrencyPairId;
import org.multibit.exchange.infrastructure.adaptor.eventapi.ExchangeId;
import org.multibit.exchange.infrastructure.adaptor.eventapi.OrderDescriptor;
import org.multibit.exchange.infrastructure.adaptor.eventapi.PlaceOrderCommand;
import org.multibit.exchange.infrastructure.adaptor.eventapi.RegisterCurrencyPairCommand;
import org.multibit.exchange.infrastructure.adaptor.eventapi.RemoveTickerCommand;
import org.multibit.exchange.infrastructure.adaptor.eventapi.SecurityOrderFactory;

import java.util.Map;

/**
 * <p>AggregateRoot to provide the following to the domain model:</p>
 * <ul>
 * <li>An Event sourced exchange.</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class Exchange extends AbstractAnnotatedAggregateRoot {

  @EventSourcedMember
  private Map<CurrencyPairId, MatchingEngine> matchingEngineMap = Maps.newHashMap();

  @AggregateIdentifier
  private ExchangeId exchangeId;

  /**
   * No-arg constructor required by Axon Framework.
   */
  @SuppressWarnings("unused")
  public Exchange() {
  }

  /*
   * Create Exchange
   */
  @CommandHandler
  @SuppressWarnings("unused")
  public Exchange(CreateExchangeCommand command) {
    apply(new ExchangeCreatedEvent(command.getExchangeId()));
  }

  @EventHandler
  public void on(ExchangeCreatedEvent event) {
    exchangeId = event.getExchangeId();
  }


  /*
   * Register Currency Pair
   */
  @CommandHandler
  @SuppressWarnings("unused")
  void registerCurrencyPair(RegisterCurrencyPairCommand command) throws DuplicateCurrencyPairSymbolException {
    checkForDuplicateCurrencyPair(command.getCurrencyPairId());

    apply(new CurrencyPairRegisteredEvent(exchangeId, command.getCurrencyPairId(), command.getBaseCurrency(), command.getCounterCurrency()));
  }

  private void checkForDuplicateCurrencyPair(CurrencyPairId symbol) throws DuplicateCurrencyPairSymbolException {
    if (matchingEngineMap.containsKey(symbol)) {
      throw new DuplicateCurrencyPairSymbolException(symbol);
    }
  }

  @EventHandler
  public void on(CurrencyPairRegisteredEvent event) throws DuplicateCurrencyPairSymbolException {
    CurrencyPairId currencyPairId = event.getCurrencyPairId();
    matchingEngineMap.put(currencyPairId, createMatchingEngineForCurrencyPair(currencyPairId, event.getBaseCurrency(), event.getCounterCurrency()));
  }

  private MatchingEngine createMatchingEngineForCurrencyPair(CurrencyPairId currencyPairId, String baseCurrency, String counterCurrency) {
    return new MatchingEngine(exchangeId, currencyPairId, baseCurrency, counterCurrency);
  }


  /*
   * Remove Currency Pair
   */
  @CommandHandler
  @SuppressWarnings("unused")
  private void removeCurrencyPair(RemoveTickerCommand command) throws NoSuchTickerException {
    validate(command);
    apply(new CurrencyPairRemovedEvent(exchangeId, command.getTickerSymbol()));
  }

  private void validate(RemoveTickerCommand command) throws NoSuchTickerException {
    String tickerSymbol = command.getTickerSymbol();
    if (!matchingEngineMap.containsKey(new CurrencyPairId(tickerSymbol))) {
      throw new NoSuchTickerException(tickerSymbol);
    }
  }

  @EventHandler
  public void on(CurrencyPairRemovedEvent event) {
    String tickerSymbol = event.getSymbol();
    matchingEngineMap.remove(new CurrencyPairId(tickerSymbol));
  }


  /*
   * Place Order
   */
  @CommandHandler
  @SuppressWarnings("unused")
  public void placeOrder(PlaceOrderCommand command) throws NoSuchTickerException {
    OrderDescriptor orderDescriptor = command.getOrderDescriptor();
    SecurityOrder order = SecurityOrderFactory.createOrderFromDescriptor(orderDescriptor);
    Ticker ticker = order.getTicker();
    if (!matchingEngineMap.containsKey(new CurrencyPairId(ticker.getSymbol()))) {
      throw new NoSuchTickerException(ticker.getSymbol());
    }

    matchingEngineMap.get(new CurrencyPairId(ticker.getSymbol())).acceptOrder(order);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Exchange exchange = (Exchange) o;

    if (exchangeId != null ? !exchangeId.equals(exchange.exchangeId) : exchange.exchangeId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return exchangeId != null ? exchangeId.hashCode() : 0;
  }
}
