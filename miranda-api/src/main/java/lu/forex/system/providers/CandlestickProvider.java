package lu.forex.system.providers;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lu.forex.system.dtos.CandlestickResponseDto;
import lu.forex.system.entities.Candlestick;
import lu.forex.system.entities.Symbol;
import lu.forex.system.enums.TimeFrame;
import lu.forex.system.mappers.CandlestickMapper;
import lu.forex.system.repositories.CandlestickRepository;
import lu.forex.system.services.CandlestickService;
import lu.forex.system.utils.TimeFrameUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Getter(AccessLevel.PRIVATE)
public class CandlestickProvider implements CandlestickService {

  private final CandlestickRepository candlestickRepository;
  private final CandlestickMapper candlestickMapper;

  @NotNull
  @Override
  public Collection<CandlestickResponseDto> getCandlesticks(@NotNull final String symbolName, @NotNull final TimeFrame timeFrame) {
    return this.getCandlestickRepository().findBySymbol_NameAndTimeFrameOrderByTimestampAsc(symbolName, timeFrame).stream()
        .map(this.getCandlestickMapper()::toDto).collect(Collectors.toList());
  }

  @Override
  public void createOrUpdateCandlestick(@NotNull final Symbol symbol, @NotNull final LocalDateTime timestamp, final double price) {
    final Collection<Candlestick> candlestickCollection = Arrays.stream(TimeFrame.values()).parallel().map(timeFrame -> {
      final LocalDateTime localTimesFrame = TimeFrameUtils.getCandlestickDateTime(timestamp, timeFrame);
      final Optional<Candlestick> lastCandlestickOptional = this.getCandlestickRepository()
          .findFirstBySymbolAndTimeFrameOrderByTimestampDesc(symbol, timeFrame);
      if (lastCandlestickOptional.isPresent() && lastCandlestickOptional.get().getTimestamp().equals(localTimesFrame)) {
        return this.updateCandlestick(price, lastCandlestickOptional.get());
      } else {
        return this.createCandlestick(symbol, price, timeFrame, localTimesFrame);
      }
    }).toList();
    this.getCandlestickRepository().saveAllAndFlush(candlestickCollection);
  }

  private @NotNull Candlestick updateCandlestick(final @Positive double price, final @Nonnull Candlestick lastCandlestick) {
    if (lastCandlestick.getHigh() < price) {
      lastCandlestick.setHigh(price);
    } else if (lastCandlestick.getLow() > price) {
      lastCandlestick.setLow(price);
    }
    lastCandlestick.setClose(price);
    return lastCandlestick;
  }

  private @NotNull Candlestick createCandlestick(final @Nonnull Symbol symbol, final @Positive double price, final @Nonnull TimeFrame timeFrame,
      final @Nonnull LocalDateTime localTimesFrame) {
    final Candlestick candlestick = new Candlestick();
    candlestick.setTimestamp(localTimesFrame);
    candlestick.setTimeFrame(timeFrame);
    candlestick.setSymbol(symbol);
    candlestick.setHigh(price);
    candlestick.setLow(price);
    candlestick.setOpen(price);
    candlestick.setClose(price);
    return candlestick;
  }
}