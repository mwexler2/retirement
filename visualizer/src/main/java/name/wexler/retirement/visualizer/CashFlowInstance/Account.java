package name.wexler.retirement.visualizer.CashFlowInstance;

import com.fasterxml.jackson.core.JsonFactory;
import name.wexler.retirement.datastore.PositionHistory;
import name.wexler.retirement.visualizer.*;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

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
import java.util.regex.Pattern;

public interface Account extends CashFlowSource, CashFlowSink {
    Entity getCompany();
    String getTxnSource();
    String getName();
    CashFlowInstance processSymbol(Context context, String symbol, String description,
                                   final @NotNull String parentCategory,
                                   final @NotNull String category,
                                   final @NotNull String itemType,
                                          BigDecimal shares, LocalDate txnDate, BigDecimal txnAmount);

    @NotNull
    default CashFlowInstance getInstance(Context context,
                                         Spending spending,
                                         ResultSet rs,
                                         CashFlowSource cashFlowSource,
                                         String description,
                                         Job job
    ) throws SQLException, AccountNotFoundException {
        CashFlowInstance instance = null;

        long dateMillis = rs.getLong("Date");
        LocalDate txnDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate accrualEnd = txnDate;
        BigDecimal txnAmount = BigDecimal.ZERO;
        long id = rs.getLong("id");
        String category = rs.getString("category");
        String notes = ObjectUtils.defaultIfNull(rs.getString("notes"), "");
        String labelsStr = ObjectUtils.defaultIfNull(rs.getString("labels"), "");
        List<String> names = getLabels(labelsStr);
        String parentCategory = rs.getString("parent");
        if (parentCategory == null)
            parentCategory = Category.UNKNOWN;
        String itemType = rs.getString("itemType");
        String txnType = rs.getString("txn_type");
        if (description.startsWith("BANK INT")) {
            category = "Interest Income";
            parentCategory = "Investment";
            itemType = "INCOME";
        } else if (description.startsWith("Tfr")) {
            category = "Transfer";
            parentCategory = "Transfer";
            itemType = "Transfer";
        } else if (description.startsWith("JOURNAL ")) {
            category = "Journal";
            parentCategory = "Transfer";
            itemType = "Transfer";
        } else if (description.startsWith("OVERDRAFT ")) {
            category = "Overdraft";
            parentCategory = "Transfer";
            itemType = "Transfer";
        } else if (description.startsWith("Check:")) {
            category = "Check";
            parentCategory = "Uncategorized";
            itemType = Category.EXPENSE;
        } else if (parentCategory.equals(Category.INVESTMENT) &&
                (description.startsWith("Interest ") || description.endsWith("ACCOUNT INTEREST"))) {
            category = "Interest Income";
            parentCategory = "Investment";
            itemType = Category.INCOME;
        } else if (description.startsWith("Olink Tid")) {
            category = "Transfer";
            parentCategory = "Investment";
            itemType = Category.TRANSFERS;
        } else if (description.equals("Payroll Contribution")) {
            category="Payroll Contribution";
            parentCategory = "Investment";
            itemType = Category.TRANSFERS;
        } else if (description.startsWith("FUNDS RECEIVED ")) {
            category = "Funds Received";
            parentCategory = "Transfer";
            itemType = "Transfer";
        } else if (description.endsWith("Plan Contribution") ||
                   description.endsWith("- Contribution") ||
                   description.contains(" CONTRIBUTION ") ||
                   description.startsWith("Contribution ")) {
            category = "Contribution";
            parentCategory = "Retirement";
            itemType = "Transfer";
        } else if (description.startsWith("WAIVE ")) {
            category = "Bank fee";
            parentCategory = "Fees & Charges";
            itemType = "Income";
        } else if (txnType.equals("CHECK")) {
            category = "Check";
            parentCategory = "Uncategorized";
            itemType = Category.EXPENSE;
        } else if (txnType.equals("FEE")) {
            category = "Bank Fee";
            parentCategory = "Fees & Charges";
            itemType = Category.EXPENSE;
        } else if (Pattern.matches("US TREASURY.*MATURED", description)) {
            category = "Principal";
            parentCategory = "Investment";
            itemType = "Income";
        } else if (description.contains("CASH DIV")) {
            category = "Dividends & Capital Gains";
            parentCategory = Category.INVESTMENT;
            itemType = "Income";
        } else if (itemType == null) {
            itemType = txnType.equals("DEBIT") ? Category.EXPENSE : txnType.equals("CREDIT") ? "Income" : "Transfer";
            parentCategory = "Uncategorized";
            category = "Uncategorized";
        }
        Boolean isDebit = rs.getBoolean("isDebit");
        Entity company = this.getCompany();
        String symbol = rs.getString("symbol");

        try {
            txnAmount = rs.getBigDecimal("amount");
        } catch (NumberFormatException nfe) {
            return null;
        }
        if (symbol != null && symbol.length() > 0) {
            BigDecimal shares = rs.getBigDecimal("shares");
            instance = processSymbol(context, symbol, description,
                    parentCategory, category, itemType, shares, txnDate, txnAmount);
        }
        if (instance != null) {
        } else if (isDebit && !itemType.equals("TRANSFER")) {
            if (company == null) {
                throw new Account.AccountNotFoundException(this.getName());
            }
            if (cashFlowSource == null)
                cashFlowSource = spending;
            instance = new PaymentInstance(id, cashFlowSource, this,
                    parentCategory, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, company, description);
        } else if (job != null && !isDebit && category.equals("Paycheck")) {
            if (company == null) {
                throw new AccountNotFoundException(this.getName());
            }
            instance = new PaycheckInstance(id, job, this,
                    parentCategory, category, accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, description);
        } else if (job != null && !isDebit && category.equals("Reimbursement")) {
            if (company == null) {
                throw new AccountNotFoundException(this.getName());
            }
            instance = new ReimbursementInstance(id, this, job.getDefaultSink(),
                    parentCategory, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, company,
                    description);
        } else {
            instance = new CashFlowInstance(id, false, this, this,
                    itemType, parentCategory, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, description);
        }
        instance.setNotes(notes);
        instance.setLabels(names);
        return instance;
    }

    private List<String> getLabels(String labelsStr) {
        List<String> names = new ArrayList<>();
        if (labelsStr != null && !labelsStr.isEmpty()) {
            try (JsonParser parser = Json.createParser(new StringReader(labelsStr))) {
                JsonParser.Event event = parser.next();
                JsonArray labels = parser.getArray();
                labels.forEach(label -> {
                    JsonObject obj = label.asJsonObject();
                    String name = obj.getString("name");
                    names.add(name);
                });
            }
        }
        return names;
    }

    void setRunningTotal(LocalDate balanceDate, BigDecimal value);

    class AccountNotFoundException extends Exception {
        private final String accountName;

        public AccountNotFoundException(String accountName) {
            super("AssetAccount " + accountName + " not found");
            this.accountName = accountName;
        }
    }
}
