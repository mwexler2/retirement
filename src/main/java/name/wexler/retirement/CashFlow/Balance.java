package name.wexler.retirement.CashFlow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * Created by mwexler on 11/29/16.
 */
public class Balance {
    private final LocalDate balanceDate;
    private final BigDecimal _value;

    public Balance(LocalDate balanceDate, BigDecimal amount) {
        this.balanceDate = balanceDate;
        this._value = amount;
    }

    public BigDecimal getValue() {
        return _value;
    }

    public LocalDate getBalanceDate() {
        return balanceDate;
    }


    @Override
    public String toString() {
        String result = balanceDate.toString() + ": " + getValue();
        return result;
    }

    public BigDecimal getBalanceAtDate(LocalDate endDate, double annualReturn) {
        Period elapsed = balanceDate.until(endDate);
        double elapsedYears = elapsed.getDays()/365.25;
        double totalReturn = Math.pow(annualReturn, elapsedYears);
        return _value.multiply(BigDecimal.valueOf(totalReturn));
    }
}
