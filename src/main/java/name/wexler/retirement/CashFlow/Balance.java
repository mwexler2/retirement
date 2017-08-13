package name.wexler.retirement.CashFlow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import name.wexler.retirement.JSONDateDeserialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Created by mwexler on 11/29/16.
 */
public class Balance {
    private final LocalDate balanceDate;
    private final BigDecimal _value;

    public Balance(@JsonDeserialize(using=JSONDateDeserialize.class) @JsonProperty("date") LocalDate balanceDate,
                   @JsonProperty("amount") BigDecimal amount) {
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
        BigDecimal balance = BigDecimal.ZERO;
        if (!balanceDate.isAfter(endDate)) {
            long days = ChronoUnit.DAYS.between(balanceDate, endDate);
            double elapsedYears = days / 365.25;
            double totalReturn = Math.pow(annualReturn, elapsedYears);
            balance = _value.multiply(BigDecimal.valueOf(totalReturn));
        }
        return balance;
    }
}
