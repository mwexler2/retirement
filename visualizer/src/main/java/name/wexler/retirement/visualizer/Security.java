package name.wexler.retirement.visualizer;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.datastore.TickerHistory;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * Created by mwexler on 6/4/17.
 */
public class Security extends Entity {
    private static Map<String, Map<LocalDate, BigDecimal>> historicalPrices = new HashMap<>();
    private static final String securitiesPath = "securities.json";
    private static TickerHistory tickerHistory = null;
    private final static BigDecimal daysInYear = BigDecimal.valueOf(365.25);
    private final static int ROUNDING_SCALE = 8;

    static public void readSecurities(Context context, DataStore ds) throws IOException {
        context.fromJSONFileList(Security[].class, securitiesPath);
        tickerHistory = ds.getTickerHistory();
        Map<String, LocalDate> tickers = tickerHistory.getTickers();
        tickers.forEach((name, date) -> {
            System.out.println("Reading " + name);
            Map<LocalDate, BigDecimal> singleTickerHistory = tickerHistory.getHistoricalPrices(name);
            historicalPrices.put(name, singleTickerHistory);
        });
    }

    @JsonCreator
    public Security(@JacksonInject("context") Context context,
                    @JsonProperty(value = "id", required = true) String id)
    throws DuplicateEntityException {
        super(context, id, Security.class);
        context.put(Security.class, id, this);
    }

    public String getName() {
        return getId();
    }

    public BigDecimal getSharePriceAtDate(LocalDate valueDate, Assumptions assumptions) {
        BigDecimal sharePrice = BigDecimal.ZERO;
        if (historicalPrices.containsKey(getId())) {
            Map<LocalDate, BigDecimal> singleTickerHistory = historicalPrices.get(getId());
            if (singleTickerHistory.containsKey(valueDate))
                sharePrice = singleTickerHistory.get(valueDate);
            else
                sharePrice = estimatePrice(singleTickerHistory, valueDate, assumptions);
        }
        return sharePrice;
    }

    public BigDecimal estimatePrice(Map<LocalDate, BigDecimal> singleTickerHistory, LocalDate valueDate, Assumptions assumptions) {
        BigDecimal sharePrice = BigDecimal.ZERO;

        LocalDate latestDate = singleTickerHistory.keySet().stream().max(LocalDate::compareTo).get();
        if (valueDate.isAfter(latestDate)) {

            long days = latestDate.until(valueDate, ChronoUnit.DAYS);
            BigDecimal growth = BigDecimal.valueOf(days).divide(daysInYear, ROUNDING_SCALE, RoundingMode.HALF_UP)
                    .multiply(assumptions.getLongTermInvestmentReturn()).add(BigDecimal.ONE);
            sharePrice = singleTickerHistory.get(latestDate).multiply(growth);
        }
        return sharePrice;
    }
}
