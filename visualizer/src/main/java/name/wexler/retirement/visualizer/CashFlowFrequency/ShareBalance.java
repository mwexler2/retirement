package name.wexler.retirement.visualizer.CashFlowFrequency;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import name.wexler.retirement.datastore.PositionHistory;
import name.wexler.retirement.visualizer.JSON.JSONDateDeserialize;
import name.wexler.retirement.visualizer.JSON.JSONDateSerialize;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Security;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Created by mwexler on 11/29/16.
 */
public class ShareBalance implements Balance  {
    @JsonDeserialize(using= JSONDateDeserialize.class)
    @JsonSerialize(using= JSONDateSerialize.class)
    private final LocalDate balanceDate;
    private final BigDecimal shares;
    private final BigDecimal sharePrice;
    private final Security security;

    public ShareBalance(LocalDate balanceDate,
                        BigDecimal shares,
                        BigDecimal sharePrice,
                        Security security) {
        this.balanceDate = balanceDate;
        this.shares = shares;
        this.sharePrice = sharePrice;
        this.security = security;
    }

    public ShareBalance(@JacksonInject("context") Context context,
                        @JsonDeserialize(using=JSONDateDeserialize.class) @JsonProperty(value = "date", required = true) LocalDate balanceDate,
                        @JsonProperty(value = "shares", defaultValue = "0.00") BigDecimal shares,
                        @JsonProperty(value = "sharePrice", required = true) BigDecimal sharePrice,
                        @JsonProperty(value = "security", required = true) String securityId) {
        this(balanceDate, shares, sharePrice, context.getById(Security.class, securityId));
    }

    public ShareBalance(Context context, PositionHistory.Position position) {
        this(context, position.getDate().toInstant().atZone(ZoneId.of("GMT")).toLocalDate(),
                position.getUnitPrice(),
                position.getUnitPrice(),
                position.getName());
    }

    public BigDecimal getValue() {
        return shares.multiply(sharePrice).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSharePrice() {
        return sharePrice;
    }

    public BigDecimal getShares() { return shares; }

    public String getId() {
        return security.getId();
    }

    public LocalDate getBalanceDate() {
        return balanceDate;
    }

    public Security getSecurity() { return security; }

    public ShareBalance applyChange(ShareBalance change) {
        BigDecimal shares = this.shares.add(change.shares).setScale(2, RoundingMode.HALF_UP);
        return new ShareBalance(this.getBalanceDate(), shares, change.sharePrice, change.getSecurity());
    }

    public BigDecimal getShareValue() {
        return shares.multiply(sharePrice);
    }

    @Override
    public String toString() {
        return balanceDate.toString() + ": " + this.security.getId() + " - " +
                this.shares + " * " + this.sharePrice + " = " + this.getValue();
    }
}
