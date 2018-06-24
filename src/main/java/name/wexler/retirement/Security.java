package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashBalance;
import name.wexler.retirement.CashFlow.ShareBalance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mwexler on 6/4/17.
 */
public class Security {
    private List<ShareBalance> sharePrices;
    private String id;

    @JsonCreator
    public Security(@JacksonInject("context") Context context,
                    @JsonProperty(value = "id", required = true) String id,
                    @JsonProperty("sharePrices") List<ShareBalance> sharePrices) {
        this.sharePrices = sharePrices;
        this.id = id;
        context.put(Security.class, id, this);
    }

    public String getName() {
        return id;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getSharePriceAtDate(LocalDate valueDate, Assumptions assumptions) {
        BigDecimal sharePrice = BigDecimal.ZERO;
        for (ShareBalance price : sharePrices) {
            if (!valueDate.isBefore(price.getBalanceDate())) {
                sharePrice = price.getSharePrice();
            }
        }
        return sharePrice;
    }
}
