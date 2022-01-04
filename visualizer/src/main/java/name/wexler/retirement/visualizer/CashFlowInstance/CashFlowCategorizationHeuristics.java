package name.wexler.retirement.visualizer.CashFlowInstance;

import name.wexler.retirement.visualizer.CashFlowEstimator.Salary;
import name.wexler.retirement.visualizer.CashFlowSource;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Entity.Category;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Spending;
import name.wexler.retirement.visualizer.Job;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CashFlowCategorizationHeuristics {
    private static JsonParserFactory jsonParserFactory = Json.createParserFactory(Collections.emptyMap());

    @NotNull
    public static CashFlowInstance getInstance(Context context,
                                               Account account,
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
                description.endsWith("ACCOUNT INTEREST")) {
            category = Category.INTEREST_INCOME;
            parentCategory = Category.INVESTMENT;
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (description.startsWith("Tfr") || category.equals("Transfer")) {
            category = Category.TRANSFER_CATEGORY;
            parentCategory = Category.TRANSFER_CATEGORY;
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("JOURNAL ")) {
            category = Category.JOURNAL_CATEGORY;
            parentCategory = Category.TRANSFER_CATEGORY;
            itemType = Category.INCOME_ITEM_TYPE;
        } else if (description.toLowerCase(Locale.ROOT).startsWith("overdraft ")) {
            category = Category.OVERDRAFT_CATEGORY;
            parentCategory = Category.TRANSFER_CATEGORY;
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("Check:")) {
            category = Category.CHECK_CATEGORY;
            parentCategory = Category.UNCATEGORIZED_CATEGORY;
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (description.startsWith("Olink Tid")) {
            category = Category.TRANSFER_CATEGORY;
            parentCategory = Category.INVESTMENT;
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.equals("Payroll Contribution")) {
            category = Category.PAYROLL_CONTRIBUTION_CATEGORY;
            parentCategory = Category.INVESTMENT;
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("FUNDS RECEIVED ")) {
            category = Category.FUNDS_RECEIVED_CATEGORY;
            parentCategory = Category.TRANSFER_CATEGORY;
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.endsWith("Plan Contribution") ||
                description.endsWith("- Contribution") ||
                description.contains(" CONTRIBUTION ") ||
                description.startsWith("Contribution ")) {
            category = Category.CONTRIBUTION_CATEGORY;
            parentCategory = Category.RETIREMENT_CATEGORY;
            itemType = Category.TRANSFER_ITEM_TYPE;
        } else if (description.startsWith("WAIVE ")) {
            category = Category.BANK_FEE_CATEGORY;
            parentCategory = Category.FEES_AND_CHARGES_CATEGORY;
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (txnType.equals("CHECK")) {
            category = Category.CHECK_CATEGORY;
            parentCategory = Category.UNCATEGORIZED_CATEGORY;
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (txnType.equals("FEE")) {
            category = Category.BANK_FEE_CATEGORY;
            parentCategory = Category.FEES_AND_CHARGES_CATEGORY;
            itemType = Category.EXPENSE_ITEM_TYPE;
        } else if (Pattern.matches("US TREASURY.*MATURED", description)) {
            category = Category.PRINCIPAL_CATEGORY;
            parentCategory = Category.INVESTMENT;
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
                category = Category.REINVEST_SHARES_CATEGORY;
            } else if (description.startsWith("REINVESTMENT")) {
                category = Category.REINVEST_DIVIDENDS_CATEGORY;
            } else {
                category = "Contribution";
            }
        } else if (txnType.equals("transfer") && shares.compareTo(BigDecimal.ZERO) == 0) {
            itemType = Category.TRANSFER_ITEM_TYPE;
            parentCategory = Category.INVESTMENT;
            category = "Dividend Reinvestment";
        }
        Boolean isDebit = rs.getBoolean("isDebit");
        Entity company = account.getCompany();

        try {
            txnAmount = rs.getBigDecimal("amount");
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
        if (symbol != null && symbol.length() > 0) {
            instance = account.processSymbol(id, context, symbol, description,
                    parentCategory, category, itemType, shares, txnDate, txnAmount);
        }
        if (instance != null) {
        } else if (isDebit && !itemType.equals("TRANSFER")) {
            if (company == null) {
                throw new AccountNotFoundException(account.getName());
            }
            if (cashFlowSource == null)
                cashFlowSource = spending;
            instance = new PaymentInstance(id, cashFlowSource, account,
                    parentCategory, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, company, description);
        } else if (job != null && !isDebit && category.equals("Paycheck")) {
            if (company == null) {
                throw new AccountNotFoundException(account.getName());
            }
            instance = new PaycheckInstance(id, job, account,
                    parentCategory, category, accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, description);
        } else if (job != null && !isDebit && category.equals("Reimbursement")) {
            if (company == null) {
                throw new AccountNotFoundException(account.getName());
            }
            instance = new ReimbursementInstance(id, account, job.getDefaultSink(),
                    parentCategory, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, company,
                    description);
        } else {
            instance = new CashFlowInstance(id, false, account, account,
                    itemType, parentCategory, category,
                    accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO, description);
        }
        instance.setNotes(notes);
        instance.setLabels(names);
        return instance;
    }

    private static List<String> getLabels(JsonParserFactory jsonParserFactory, String labelsStr) {
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

    public static class AccountNotFoundException extends Exception {
        private final String accountName;

        public AccountNotFoundException(String accountName) {
            super("AssetAccount " + accountName + " not found");
            this.accountName = accountName;
        }
    }
}
