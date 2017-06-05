package name.wexler.retirement.CashFlow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by mwexler on 11/29/16.
 */
public class AssetValue {
    private final LocalDate _assetValueDate;
    private final BigDecimal _value;

    public AssetValue(LocalDate assetValueDate, BigDecimal amount) {
        this._assetValueDate = assetValueDate;
        this._value = amount;
    }

    public BigDecimal getValue() {
        return _value;
    }

    public LocalDate getAssetValueDate() {
        return _assetValueDate;
    }


    @Override
    public String toString() {
        String result = _assetValueDate.toString() + ": " + getValue();
        return result;
    }
}
