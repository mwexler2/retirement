package name.wexler.retirement.visualizer.CashFlowInstance;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.Context;

import java.math.BigDecimal;


public class SecurityTransaction extends AssetTransaction {
    private final ShareBalance change;
    private final AssetAccount account;

    public SecurityTransaction(
            Context context,
            AssetAccount account,
            String itemType,
            String category,
            BigDecimal amount,
            ShareBalance shareChange) {
        super(false,
                account,
                account,
                itemType, category,
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(), amount, BigDecimal.ZERO);
        change = shareChange;
        this.account = account;
    }

    public ShareBalance getChange() {
        return change;
    }

    public String toString() {
        return this.getCashFlowDate() + ": " + change.getSecurity().getId() + " - " +
                change.getShares() + " * $" + change.getSharePrice();
    }
}
