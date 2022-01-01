package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.Context;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;


public class SecurityTransaction extends AssetTransaction {
    private final ShareBalance change;
    private final AssetAccount account;

    public SecurityTransaction(
            long id,
            Context context,
            AssetAccount account,
            final @NotNull String itemType,
            final @NotNull String parentCategory,
            final @NotNull String category,
            BigDecimal amount,
            ShareBalance shareChange,
            String description) {
        super(id,
                false,
                account,
                account,
                itemType, parentCategory, category,
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(),
                shareChange.getBalanceDate(), amount, BigDecimal.ZERO,
                description);
        change = shareChange;
        this.account = account;
    }

    public String getSymbol() {
        return change.getSecurity().getName();
    }

    public BigDecimal getUnits() {
        return change.getShares();
    }

    public BigDecimal getUnitPrice() {
        return change.getSharePrice();
    }

    public ShareBalance getChange() {
        return change;
    }

    public String toString() {
        return this.getCashFlowDate() + ": " + change.getSecurity().getId() + " - " +
                change.getShares() + " * $" + change.getSharePrice();
    }
}
