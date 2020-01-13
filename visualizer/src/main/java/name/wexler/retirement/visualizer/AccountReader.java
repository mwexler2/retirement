package name.wexler.retirement.visualizer;

import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.datastore.PositionHistory;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.CreditCardAccount;
import name.wexler.retirement.visualizer.CashFlowEstimator.SecuredLoan;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Expense;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.apache.activemq.util.IOExceptionHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.openjpa.jdbc.kernel.exps.MapEntry;

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
    private Spending spending;

    public AccountReader(Context context) {
        spending = context.getById(Expense.class, "spending");
    }

    public class AccountNotFoundException extends Exception {
        private final String accountName;

        public AccountNotFoundException(String accountName) {
            super("AssetAccount " + accountName + " not found");
            this.accountName = accountName;
        }
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
            long dateMillis = rs.getLong("Date");
            LocalDate txnDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("UTC")).toLocalDate();
            LocalDate accrualEnd = txnDate;
            BigDecimal txnAmount = BigDecimal.ZERO;
            String action = rs.getString("txn_type");

            String description = rs.getString("description");
            String category = rs.getString("category");
            String notes = ObjectUtils.defaultIfNull(rs.getString("notes"), "");
            String labelsStr = ObjectUtils.defaultIfNull(rs.getString("labels"), "");
            String itemType = rs.getString("itemType");

            Boolean isBuy = rs.getBoolean("isBuy");
            Boolean isCheck = rs.getBoolean("isCheck");
            Boolean isChild = rs.getBoolean("isChild");
            Boolean isDebit = rs.getBoolean("isDebit");
            Boolean isDuplicate = rs.getBoolean("isDuplicate");
            Boolean isEdited = rs.getBoolean("isEdited");
            Boolean isFirstDate = rs.getBoolean("isFirstDate");
            Boolean isLinkedToRule = rs.getBoolean("isLinkedToRule");
            Boolean isMatched = rs.getBoolean("isMatched");
            Boolean isPending = rs.getBoolean("isPending");
            Boolean isPercent = rs.getBoolean("isPercent");
            Boolean isSell = rs.getBoolean("isSell");
            Boolean isSpending = rs.getBoolean("isSpending");
            Boolean isTransfer = rs.getBoolean("isTransfer");

            List<String> labels = Arrays.asList(labelsStr.split(","));

            Account account = getAccountFromResultSet(context, rs, txnDate);
            if (account == null)
                return null;

            try {
                txnAmount = rs.getBigDecimal("amount");
            } catch (NumberFormatException nfe) {
                return null;
            }

            CashFlowInstance instance;
            Entity company = account.getCompany();
            CashFlowSource cashFlowSource = context.getById(Account.class, description);
            Job job = getJobFromDescription(context, description);
            if (account instanceof AssetAccount && (category.equals("Sell") || category.equals("Buy"))) {
                AssetAccount assetAccount = (AssetAccount) account;
                String symbol = rs.getString("symbol");
                BigDecimal shares = rs.getBigDecimal("shares");
                BigDecimal sharePrice = null;
                Security security;
                if (symbol == null || symbol.equals("")) {
                    Pattern p1 = Pattern.compile("(\\d+(\\.\\d+)?) of ([^ ]+) @ \\$(\\d+\\.\\d+).*", Pattern.CASE_INSENSITIVE);
                    Pattern p2 = Pattern.compile("YOU BOUGHT ([^ ]+).*");
                    Matcher m = p1.matcher(description.replace(",", ""));
                    if (m.matches()) {
                        shares = BigDecimal.valueOf(Double.parseDouble(m.group(1)));
                        if (category.equals("Buy"))
                            shares = shares.negate();
                        symbol = m.group(3);
                        sharePrice = BigDecimal.valueOf(Double.parseDouble(m.group(4)));
                    } else {
                        m = p2.matcher(description);
                        if (m.matches())
                            symbol = m.group(1);
                    }
                }
                if (symbol == null || symbol.equals("")) {
                    System.err.println("No security specified in " + description);
                    return null;
                } else {
                    security = context.getById(Security.class, symbol);
                    try {
                        if (security == null)
                            security = new Security(context, symbol);
                    } catch (Entity.DuplicateEntityException dee) {
                        throw(new RuntimeException(dee));
                    }
                    if (sharePrice == null) {
                        if (shares == null || shares.compareTo(BigDecimal.ZERO) == 0) {
                            shares = BigDecimal.ZERO;
                            sharePrice = BigDecimal.ZERO;
                        } else {
                            sharePrice = txnAmount.divide(shares, 2, RoundingMode.HALF_UP).abs();
                        }
                    }
                    ShareBalance shareChange = new ShareBalance(txnDate, shares, sharePrice, security);
                    instance = new SecurityTransaction(context, assetAccount, itemType, category, txnAmount, shareChange);
                }
            } else if (isDebit) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(account.getName()));
                    return null;
                }
                if (cashFlowSource == null)
                    cashFlowSource = spending;
                instance = new PaymentInstance(cashFlowSource, account, category,
                        accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO, company);
            } else if (job != null && !isDebit && category.equals("Paycheck")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(account.getName()));
                    return null;
                }
                instance = new PaycheckInstance(job, account, category, accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO);
            } else if (job != null && !isDebit && category.equals("Reimbursement")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(account.getName()));
                    return null;
                }
                instance = new ReimbursementInstance(account, job.getDefaultSink(), category,
                        accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO, company);
            } else {
                instance = new CashFlowInstance(false, account, account,
                        itemType, category,
                        accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO);
            }

            instance.setDescription(description);
            instance.setCategory(category);
            instance.setNotes(notes);
            instance.setLabels(labels);
            return instance;
        } catch (SQLException sqle) {
            System.err.println(sqle);
        } catch (AccountNotFoundException anfe) {
            System.err.println(anfe);
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
    private Account getAccountFromResultSet(Context context, ResultSet rs, LocalDate txnDate)
            throws AccountNotFoundException, SQLException {
        Account account = null;
        String accountName = rs.getString("account_name").trim();
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
}
