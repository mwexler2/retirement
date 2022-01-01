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
    static JsonParserFactory jsonParserFactory = Json.createParserFactory(Collections.emptyMap());
    CashFlowInstance processSymbol(long id,
                                   Context context, String symbol, String description,
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
        String fi = rs.getString("fi");
        String category = rs.getString("category");
        String notes = ObjectUtils.defaultIfNull(rs.getString("notes"), "");
        String labelsStr = ObjectUtils.defaultIfNull(rs.getString("labels"), "");
        List<String> names = getLabels(jsonParserFactory, labelsStr);
        String parentCategory = rs.getString("parent");
        if (parentCategory == null)
            parentCategory = Category.UNKNOWN;
        String itemType = rs.getString("itemType");
        String txnType = rs.getString("txn_type");
        String symbol = rs.getString("symbol");
        BigDecimal shares = rs.getBigDecimal("shares");
        if (description.toLowerCase(Locale.ROOT).startsWith("bank int") ||
                description.toLowerCase(Locale.ROOT).startsWith("interest ") ||
                description.endsWith("ACCOUNT INTEREST"))  {
            category = "Interest Income";
            parentCategory = "Investment";
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (description.startsWith("Tfr") || category.equals("Transfer")) {
            category = "Transfer";
            parentCategory = "Transfer";
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("JOURNAL ")) {
            category = "Journal";
            parentCategory = "Transfer";
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (description.toLowerCase(Locale.ROOT).startsWith("overdraft ")) {
            category = "Overdraft";
            parentCategory = "Transfer";
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("Check:")) {
            category = "Check";
            parentCategory = "Uncategorized";
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (description.startsWith("Olink Tid")) {
            category = "Transfer";
            parentCategory = "Investment";
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.equals("Payroll Contribution")) {
            category="Payroll Contribution";
            parentCategory = "Investment";
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("FUNDS RECEIVED ")) {
            category = "Funds Received";
            parentCategory = "Transfer";
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.endsWith("Plan Contribution") ||
                   description.endsWith("- Contribution") ||
                   description.contains(" CONTRIBUTION ") ||
                   description.startsWith("Contribution ")) {
            category = "Contribution";
            parentCategory = "Retirement";
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("WAIVE ")) {
            category = "Bank fee";
            parentCategory = "Fees & Charges";
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (txnType.equals("CHECK")) {
            category = "Check";
            parentCategory = "Uncategorized";
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (txnType.equals("FEE")) {
            category = "Bank Fee";
            parentCategory = "Fees & Charges";
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (Pattern.matches("US TREASURY.*MATURED", description)) {
            category = "Principal";
            parentCategory = "Investment";
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (description.contains("CASH DIV")) {
            category = "Dividends & Capital Gains";
            parentCategory = Category.INVESTMENT;
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (itemType == null) {
            itemType = txnType.equals("DEBIT") ? Category.EXPENSE_ITEM_TYPE : txnType.equals("CREDIT") ?
                    Category.INCOME_ITEM_TYPE : Category.EXPENSE_ITEM_TYPE;
            parentCategory = "Uncategorized";
            category = "Uncategorized";
        } else if (description.toLowerCase(Locale.ROOT).equals("twist bioscience")) {
            parentCategory = Salary.SALARY_CATEGORY;
            itemType = Category.INCOME_ITEM_TYPE;
            category = Salary.SALARY_CATEGORY;
        } else if (description.toLowerCase(Locale.ROOT).contains("morgan stanley ach") ||
                description.toLowerCase(Locale.ROOT).contains("moneylink")) {
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (txnType.equals("transfer") && category.equals("Investments")) {
            itemType = Category.TRANSFER_ITEM_TYPE;
            parentCategory = "Investments";
            if (shares != null && !shares.equals(BigDecimal.ZERO)) {
                category = "Reinvest Shares";
            } else if (description.startsWith("REINVESTMENT")) {
                category = "Dividend Reinvestment";
            } else {
                category = "Contribution";
            }
        } else if (txnType.equals("transfer") && shares.compareTo(BigDecimal.ZERO) == 0) {
            itemType = Category.TRANSFER_ITEM_TYPE;
            parentCategory = Category.INVESTMENT;
            category = "Dividend Reinvestment";
        }
        Boolean isDebit = rs.getBoolean("isDebit");
        Entity company = this.getCompany();

        try {
            txnAmount = rs.getBigDecimal("amount");
        } catch (NumberFormatException nfe) {
            return null;
        }
        if (symbol != null && symbol.length() > 0) {
            instance = processSymbol(id, context, symbol, description,
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

    private List<String> getLabels(JsonParserFactory jsonParserFactory, String labelsStr) {
        List<String> names = new ArrayList<>();
        if (labelsStr != null && !labelsStr.isEmpty()) {
            try (JsonParser parser = jsonParserFactory.createParser(new StringReader(labelsStr))) {
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
