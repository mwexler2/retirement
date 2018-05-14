package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.CashFlow.Balance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mwexler on 6/4/17.
 */
public class Security extends Asset {

    @JsonCreator
    public Security(@JacksonInject("context") Context context,
                    @JsonProperty("id") String id,
                    @JsonProperty("owners") List<String> ownerIds,
                    @JsonProperty("initialAssetValue") Balance initialAssetValue,
                    @JsonProperty("interimBalances") List<Balance> interimBalances) {
        super(context, id, ownerIds, initialAssetValue, interimBalances);
    }

    public String getName() {
        return getId();
    }

    public Balance getBalanceAtDate(LocalDate valueDate, Assumptions assumptions) {
        return new Balance(valueDate, BigDecimal.valueOf(1500.00));
    }
}
