package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.CashFlow.Balance;
import name.wexler.retirement.CashFlow.CashBalance;
import name.wexler.retirement.CashFlow.ShareBalance;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.opencsv.CSVReader;


/**
 * Created by mwexler on 6/4/17.
 */
public class Security {
    private Map<LocalDate, BigDecimal> historicalPrices = new HashMap<>();
    private String id;

    @JsonCreator
    public Security(@JacksonInject("context") Context context,
                    @JsonProperty(value = "id", required = true) String id) {
        this.id = id;
        context.put(Security.class, id, this);
        _getHistory();
    }

    public String getName() {
        return id;
    }

    public String getId() {
        return id;
    }

    private String _getCSVFileName() {
        String userHome = System.getProperty("user.home");
        String resourceDir = userHome + "/.retirement/history";
        String fileName = resourceDir + "/"  + this.getId() + ".csv";

        return fileName;
    }

    private void _getHistory() {
        String csvFile = _getCSVFileName();

        CSVReaderHeaderAware reader = null;
        try {
            reader = new CSVReaderHeaderAware(new FileReader(csvFile));
            Map<String,String> line;
            while ((line =  reader.readMap()) != null) {
                if (!line.containsKey("Date") || !line.containsKey("Close"))
                    continue;
                LocalDate date = LocalDate.parse(line.get("Date"), DateTimeFormatter.ISO_DATE);
                BigDecimal close = BigDecimal.ZERO;
                try {
                    close = new BigDecimal(line.get("Close"));
                } catch ( NumberFormatException nfe) {
                    continue;   // skip invalid data (e.g. "1981-08-10,null,null,null,null,null,null")
                }
                historicalPrices.put(date, close);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getSharePriceAtDate(LocalDate valueDate, Assumptions assumptions) {
        BigDecimal sharePrice = BigDecimal.ZERO;
        if (historicalPrices.containsKey(valueDate))
            sharePrice = historicalPrices.get(valueDate);
        return sharePrice;
    }
}
