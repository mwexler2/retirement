package name.wexler.retirement.visualizer;

import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.datastore.PositionHistory;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowInstance.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.CreditCardAccount;
import name.wexler.retirement.visualizer.CashFlowEstimator.SecuredLoan;
import name.wexler.retirement.visualizer.Expense.Expense;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;


public class AccountReader {
    public static final String mintTxnSource = "mint";
    public static final String ofxTxnSource = "OFX";
    private Spending spending;

    public AccountReader(Context context) {
        spending = context.getById(Expense.class, "spending");
    }

    public List<CashFlowInstance> readCashFlowInstances(Context context) throws IOException {
        List<CashFlowInstance> cashFlowInstances = null;
        DataStore ds = Retirement.getDataStore();
        try (ResultSet rs = ds.getTxnHistory().getTransactions()) {
            cashFlowInstances = readCashFlowInstancesFromResultSet(context, rs);
            return cashFlowInstances;
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return cashFlowInstances;
    }

    public @NotNull
    List<Budget> readBudgets(Context context) throws IOException {
        List<Budget> budgets;
        DataStore ds = Retirement.getDataStore();
        try (ResultSet rs = ds.getBudgets().getBudgets()) {
            budgets = readBudgetsFromResultSet(context, rs);
            return budgets;
        } catch (SQLException sqle) {
            throw new RuntimeException("readBugets", sqle);
        }
    }

    public void getAccountBalances(Context context) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, BigDecimal> currentBalances = new HashMap<>();
        DataStore ds = Retirement.getDataStore();
        try (ResultSet rs = ds.getAccountTable().getAccounts()) {
            while (rs.next()) {
                String accountName = rs.getString("userName");
                if (accountName == null)
                    accountName = rs.getString("yodleeName");
                BigDecimal value = rs.getBigDecimal("value");
                String balanceDateStr = rs.getString("lastUpdatedInDate");
                LocalDate balanceDate = LocalDate.parse(balanceDateStr, formatter);
                CashFlowSink sink = context.getById(AssetAccount.class, accountName);
                if (sink instanceof AssetAccount) {
                    AssetAccount assetAccount = (AssetAccount) sink;
                    String accountId = assetAccount.getAccountId();
                    Map<String, PositionHistory.Position> positions = ds.getPositionHistory().getAccountPositions(accountId);
                    assetAccount.setRunningTotal(balanceDate, value, positions);
                }
                else
                    System.err.println("Skipping accountBalance for " + accountName + " not an asset account.");
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }


    private List<CashFlowInstance> readCashFlowInstancesFromResultSet (
            Context context,
            ResultSet rs) {
        List<CashFlowInstance> cashFlowInstances = new ArrayList<>();

        try {
            while (rs.next()) {
                CashFlowInstance instance = getInstanceFromResultSet(context, rs);
                if (instance != null)
                    cashFlowInstances.add(instance);
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return cashFlowInstances;
    }

    Map<String, String> txnTypeMap = Map.ofEntries(
            entry("0", "Debit"),
            entry("1", "Credit"),
            entry("2", "Transfer")
    );

    protected CashFlowInstance getInstanceFromResultSet(
            Context context,
            ResultSet rs)  {
        try {
            Account account = getAccountFromResultSet(context, rs);
            if (account == null)
                return null;
            String txnSource = rs.getString("source");
            if (!txnSource.equalsIgnoreCase(account.getTxnSource()))
                return null;    // Skip transactions from the non-authoritative source.

            String description = rs.getString("description");
            CashFlowSource cashFlowSource = context.getById(Account.class, description);
            if (cashFlowSource == null)
                cashFlowSource = spending;
            Job job = getJobFromDescription(context, description);

            CashFlowInstance instance =
                    account.getInstance(context, spending, rs, cashFlowSource, description, job);

            return instance;
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return null;
    }

    private Job getJobFromDescription(Context context, String description) {
        String companyName = description;
        if (description.toLowerCase().contains("amazon")) {
            companyName = "Amazon.com";
        } else if (description.toLowerCase().contains("yahoo")) {
            companyName = "Yahoo";
        } else if (description.toLowerCase().contains("invitae")) {
            companyName = "Invitae";
        }
        Job job = context.getById(Job.class, companyName);
        return job;
    }
    private Account getAccountFromResultSet(Context context, ResultSet rs)
            throws SQLException {
        Account account = null;
        String accountName = rs.getString("account_name").trim();
        if (accountName.equals("Investor Checking")) {
            System.out.println("Investor Checking!");
        }
        CreditCardAccount creditCardAccount = context.getById(CreditCardAccount.class, accountName);
        SecuredLoan securedLoan = context.getById(SecuredLoan.class, accountName);
        if (creditCardAccount != null) {
            account = creditCardAccount;
        } else if (securedLoan != null) {
            account = securedLoan;
        } else {
            AssetAccount assetAccount = context.getById(AssetAccount.class, accountName);
            if (assetAccount == null) {
                System.err.println("Asset Account: " + accountName + " not found.");
                return null;
            }
            account = assetAccount;
        }
        return account;
    }

    private @NotNull List<Budget> readBudgetsFromResultSet (
            Context context,
            ResultSet rs) {
        List<Budget> budgets = new ArrayList<>();

        try {
            while (rs.next()) {
                Budget budget = getBudgetFromResultSet(context, rs);
                if (budget != null)
                    budgets.add(budget);
            }
        } catch (SQLException sqle) {
            throw new RuntimeException("readBudgetsFromResultSet", sqle);
        }
        return budgets;
    }


    protected @NotNull
    Budget getBudgetFromResultSet(
            Context context,
            ResultSet rs)  {
        try {
            Boolean isIncome = rs.getBoolean("isIncome");
            Boolean isTransfer = rs.getBoolean("isTransfer");
            Boolean isExpense = rs.getBoolean("isExpense");
            BigDecimal amount = rs.getBigDecimal("amt");
            BigDecimal budget = rs.getBigDecimal("bgt");
            BigDecimal rBal = rs.getBigDecimal("rbal");
            String category = rs.getString("cat");
            String grouping = rs.getString("grouping");
            String parentCategory = rs.getString("parent");

            Budget budgetEntry =
                    new Budget(context, grouping, isIncome, isTransfer, isExpense, amount, budget, rBal, parentCategory, category);

            return budgetEntry;
        } catch (SQLException sqle) {
            throw new RuntimeException("getBudgetFromResultSet", sqle);
        }
    }
}
