package lu.forex.system.providers;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lu.forex.system.dtos.TickCreateDto;
import lu.forex.system.dtos.TickResponseDto;
import lu.forex.system.entities.Symbol;
import lu.forex.system.entities.Tick;
import lu.forex.system.exceptions.SymbolNotFoundException;
import lu.forex.system.exceptions.TickConflictException;
import lu.forex.system.exceptions.TickExistException;
import lu.forex.system.mappers.TickMapper;
import lu.forex.system.repositories.SymbolRepository;
import lu.forex.system.repositories.TickRepository;
import lu.forex.system.services.TickService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Getter(AccessLevel.PRIVATE)
public class TickProvider implements TickService {

  private final TickRepository tickRepository;
  private final SymbolRepository symbolRepository;
  private final TickMapper tickMapper;

  @NotNull
  @Override
  public Collection<@NotNull TickResponseDto> getTicks(@NotNull final String symbolName) {
    return this.getTickRepository().findBySymbol_NameOrderByTimestampAsc(symbolName).stream().map(this.getTickMapper()::toDto).toList();
  }

  @NotNull
  @Override
  public TickResponseDto addTick(@NotNull final TickCreateDto tickCreateDto, @NotNull final String symbolName) {
    final Symbol symbol = this.getSymbolByName(symbolName);
    this.validateTickNotExist(tickCreateDto, symbol);
    final Optional<Tick> optionalTick = this.findLatestTickBySymbolName(symbolName);
    if (optionalTick.isPresent() && optionalTick.get().getTimestamp().isAfter(tickCreateDto.timestamp())) {
      throw new TickConflictException(symbolName, tickCreateDto.timestamp(), optionalTick.get().getTimestamp());
    } else {
      final Tick tick = this.createTickFromDto(tickCreateDto, symbol);
      final Tick saved = this.getTickRepository().saveAndFlush(tick);
      return this.getTickMapper().toDto(saved);
    }
  }

  @Nonnull
  private Symbol getSymbolByName(final @Nonnull @NotBlank @Size(min = 6, max = 6) String symbolName) {
    return this.getSymbolRepository().findFirstByNameOrderByNameAsc(symbolName).orElseThrow(() -> new SymbolNotFoundException(symbolName));
  }

  private void validateTickNotExist(final @Nonnull TickCreateDto tickCreateDto, final @Nonnull Symbol symbol) {
    if (this.getTickRepository().existsBySymbol_NameAndTimestamp(symbol.getName(), tickCreateDto.timestamp())) {
      throw new TickExistException(tickCreateDto, symbol);
    }
  }

  @Nonnull
  private Optional<@NotNull Tick> findLatestTickBySymbolName(final @Nonnull @NotBlank @Size(min = 6, max = 6) String symbolName) {
    return this.getTickRepository().findFirstBySymbol_NameOrderByTimestampDesc(symbolName);
  }

  @Nonnull
  private Tick createTickFromDto(final @Nonnull TickCreateDto tickCreateDto, final @Nonnull Symbol symbol) {
    final Tick tick = this.getTickMapper().toEntity(tickCreateDto);
    tick.setSymbol(symbol);
    return tick;
  }
}