package name.wexler.retirement.visualizer.Tables;

import java.math.BigDecimal;

public class AmountAndLink {
    private final BigDecimal amount;
    private final String link;

    public AmountAndLink(BigDecimal amount, String link) {
        this.amount = amount;
        this.link = link;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getLink() {
        return link;
    }
}
