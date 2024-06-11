package lu.forex.system.services;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import lu.forex.system.dtos.NewTickDto;
import lu.forex.system.dtos.SymbolDto;
import lu.forex.system.dtos.TickDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface TickService {

  @Transactional()
  @Nonnull
  TickDto addTickBySymbol(final @NotNull NewTickDto newTickDto, final @NotNull SymbolDto symbolDto);

  @Transactional(readOnly = true)
  @NotNull
  Collection<@NotNull TickDto> getTicksBySymbol(final @NotNull SymbolDto symbolDto);
}
