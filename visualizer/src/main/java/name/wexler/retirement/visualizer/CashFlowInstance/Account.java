package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

public interface Account extends CashFlowSource, CashFlowSink {
    Entity getCompany();
    String getTxnSource();
    String getName();
    CashFlowInstance processSymbol(long id,
                                   Context context, String symbol, String description,
                                   final @NotNull String parentCategory,
                                   final @NotNull String category,
                                   final @NotNull String itemType,
                                          BigDecimal shares, LocalDate txnDate, BigDecimal txnAmount);

    void setRunningTotal(LocalDate balanceDate, BigDecimal value);
}
