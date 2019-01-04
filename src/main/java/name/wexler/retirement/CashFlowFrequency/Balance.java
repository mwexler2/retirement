package name.wexler.retirement.CashFlowFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public interface Balance {
    BigDecimal getValue();
    LocalDate getBalanceDate();
}
