package name.wexler.retirement.CashFlow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public interface Balance {
    abstract public BigDecimal getValue();
    abstract public LocalDate getBalanceDate();
}
