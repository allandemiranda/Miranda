package lu.forex.system.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyClass;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import lu.forex.system.enums.Indicator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "candlestick")
public class Candlestick implements Serializable {

  @Serial
  private static final long serialVersionUID = 8655855891835745603L;

  @EmbeddedId
  private CandlestickHead head;

  @Embedded
  private CandlestickBody body;

  @Exclude
  @OneToMany(cascade = CascadeType.ALL)
  private Set<MovingAverage> movingAverages = new LinkedHashSet<>();

  @Exclude
  @OneToMany(cascade = CascadeType.ALL)
  @MapKeyClass(Indicator.class)
  private Map<Indicator, TechnicalIndicator> indicators = new EnumMap<>(Indicator.class);

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Candlestick that = (Candlestick) o;
    return Objects.equals(this.getHead(), that.getHead());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getHead());
  }
}