package name.wexler.retirement.visualizer.CashFlowFrequency;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.visualizer.JSON.JSONDateDeserialize;
import name.wexler.retirement.visualizer.JSON.JSONDateSerialize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Created by mwexler on 11/29/16.
 */
public class CashBalance implements Balance {
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private LocalDate balanceDate;
    private BigDecimal value;

    public CashBalance(@JsonDeserialize(using=JSONDateDeserialize.class) @JsonProperty("date") LocalDate balanceDate,
                       @JsonProperty("amount") BigDecimal amount) {
        this.balanceDate = balanceDate;
        this.value = amount;
    }

    public BigDecimal getValue() {
        return value;
    }

    public LocalDate getBalanceDate() {
        return balanceDate;
    }


    @Override
    public String toString() {
        String result = balanceDate.toString() + ": $" + getValue();
        return result;
    }

    public BigDecimal getBalanceAtDate(LocalDate endDate, double annualReturn) {
        BigDecimal balance = BigDecimal.ZERO;
        if (!balanceDate.isAfter(endDate)) {
            long days = ChronoUnit.DAYS.between(balanceDate, endDate);
            double elapsedYears = days / 365.25;
            double totalReturn = Math.pow(annualReturn, elapsedYears);
            balance = value.multiply(BigDecimal.valueOf(totalReturn));
        }
        return balance;
    }

    public void applyChange(LocalDate balanceDate, BigDecimal amount) {
        this.balanceDate = balanceDate;
        this.value = this.value.add(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
