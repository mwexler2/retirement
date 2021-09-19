package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.apache.commons.lang3.ObjectUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public interface Account extends CashFlowSource, CashFlowSink {

    Entity getCompany();
    String getTxnSource();
    String getName();
    BigDecimal adjustAmount(BigDecimal amount);
    CashFlowInstance processSymbol(Context context, String symbol, String description, String category, String itemType,
                                          BigDecimal shares, LocalDate txnDate, BigDecimal txnAmount);

    public default CashFlowInstance getInstance(Context context,
                                                Spending spending,
                                                ResultSet rs,
                                                CashFlowSource cashFlowSource,
                                                String description,
                                                Job job) throws SQLException {
        CashFlowInstance instance = null;

        long dateMillis = rs.getLong("Date");
        LocalDate txnDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate accrualEnd = txnDate;
        BigDecimal txnAmount = BigDecimal.ZERO;
        String category = rs.getString("category");
        String notes = ObjectUtils.defaultIfNull(rs.getString("notes"), "");
        String labelsStr = ObjectUtils.defaultIfNull(rs.getString("labels"), "");
        List<String> names = new ArrayList<>();
        if (labelsStr != null && !labelsStr.isEmpty()) {
            try (JsonParser parser = Json.createParserFactory((Map<String, ?>) Collections.EMPTY_MAP).createParser(new StringReader(labelsStr))) {
                JsonArray labels = parser.getArray();
                labels.forEach(label -> {
                    JsonObject obj = label.asJsonObject();
                    String name = obj.getString("name");
                    names.add(name);
                });
            }
        }
        Category c = context.getById(Category.class, category);
        String itemType = c.getItemType();
        Boolean isDebit = rs.getBoolean("isDebit");
        Entity company = this.getCompany();
        String symbol = rs.getString("symbol");

        try {
            txnAmount = rs.getBigDecimal("amount");
        } catch (NumberFormatException nfe) {
            return null;
        }
        txnAmount = this.adjustAmount(txnAmount);
        if (symbol != null && symbol.length() > 0) {
            BigDecimal shares = rs.getBigDecimal("shares");
            instance = processSymbol(context, symbol, description, category, itemType, shares, txnDate, txnAmount);
        }
        if (instance != null) {
        } else if (isDebit) {
            if (company == null) {
                System.err.println(new Account.AccountNotFoundException(this.getName()));
                return null;
            }
            if (cashFlowSource == null)
                cashFlowSource = spending;
            instance = new PaymentInstance(cashFlowSource, this, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, company, description);
        } else if (job != null && !isDebit && category.equals("Paycheck")) {
            if (company == null) {
                System.err.println(new AccountNotFoundException(this.getName()));
                return null;
            }
            instance = new PaycheckInstance(job, this, category, accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, description);
        } else if (job != null && !isDebit && category.equals("Reimbursement")) {
            if (company == null) {
                System.err.println(new AccountNotFoundException(this.getName()));
                return null;
            }
            instance = new ReimbursementInstance(this, job.getDefaultSink(), category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, company,
                    description);
        } else {
            instance = new CashFlowInstance(false, this, this,
                    itemType, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, description);
        }
        instance.setCategory(category);
        instance.setNotes(notes);
        instance.setLabels(names);
        return instance;
    }

    public class AccountNotFoundException extends Exception {
        private final String accountName;

        public AccountNotFoundException(String accountName) {
            super("AssetAccount " + accountName + " not found");
            this.accountName = accountName;
        }
    }
}
