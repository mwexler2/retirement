package name.wexler.retirement.CashFlow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public class LiabilityValue {
    private final LocalDate _liabilityValueDate;
    private final BigDecimal _value;

    public LiabilityValue(LocalDate assetValueDate, BigDecimal amount) {
        this._liabilityValueDate = assetValueDate;
        this._value = amount;
    }

    public BigDecimal getValue() {
        return _value;
    }

    public LocalDate getLiabilityValueDate() {
        return _liabilityValueDate;
    }


    @Override
    public String toString() {
        String result = _liabilityValueDate.toString() + ": " + getValue();
        return result;
    }
}
