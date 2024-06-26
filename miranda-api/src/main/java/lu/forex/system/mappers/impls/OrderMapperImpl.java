package lu.forex.system.mappers.impls;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lu.forex.system.dtos.OrderDto;
import lu.forex.system.entities.Order;
import lu.forex.system.entities.Trade;
import lu.forex.system.mappers.OrderMapper;
import lu.forex.system.mappers.ScopeMapper;
import lu.forex.system.mappers.TickMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Getter(AccessLevel.PRIVATE)
public class OrderMapperImpl implements OrderMapper {

  private final TickMapper tickMapper;
  private final ScopeMapper scopeMapper;

  @Override
  public @NotNull Order toEntity(final @NotNull OrderDto orderDto) {
    final var order = new Order();
    final var trade = this.orderDtoToTrade(orderDto);
    order.setTrade(trade);
    order.setId(orderDto.id());
    final var openTick = this.getTickMapper().toEntity(orderDto.openTick());
    order.setOpenTick(openTick);
    final var closeTick = this.getTickMapper().toEntity(orderDto.closeTick());
    order.setCloseTick(closeTick);
    order.setOrderType(orderDto.orderType());
    order.setOrderStatus(orderDto.orderStatus());
    order.setProfit(orderDto.profit());
    return order;
  }

  @Override
  public @NotNull OrderDto toDto(final @NotNull Order order) {
    final var tradeId = this.orderTradeId(order);
    final var id = order.getId();
    final var openTick = this.getTickMapper().toDto(order.getOpenTick());
    final var closeTick = this.getTickMapper().toDto(order.getCloseTick());
    final var orderType = order.getOrderType();
    final var orderStatus = order.getOrderStatus();
    final var profit = order.getProfit();
    return new OrderDto(id, openTick, closeTick, orderType, orderStatus, profit, tradeId);
  }

  private @NotNull Trade orderDtoToTrade(final @NotNull OrderDto orderDto) {
    final var trade = new Trade();
    trade.setId(orderDto.tradeId());
    return trade;
  }

  private @NotNull UUID orderTradeId(final @NotNull Order order) {
    return order.getTrade().getId();
  }
}

