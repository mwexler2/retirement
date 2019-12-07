package name.wexler.retirement.visualizer;

import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowInstance.*;
import name.wexler.retirement.visualizer.CashFlowEstimator.CreditCardAccount;
import name.wexler.retirement.visualizer.CashFlowEstimator.SecuredLoan;
import name.wexler.retirement.visualizer.Entity.Entity;
import name.wexler.retirement.visualizer.Expense.Expense;
import name.wexler.retirement.visualizer.Expense.Spending;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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
            LocalDate txnDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate accrualEnd = txnDate;
            BigDecimal txnAmount = BigDecimal.ZERO;
            String action = rs.getString("txn_type");

            String description = rs.getString("description");
            String category = rs.getString("category");
            String notes = ObjectUtils.defaultIfNull(rs.getString("notes"), "");
            String labelsStr = ObjectUtils.defaultIfNull(rs.getString("labels"), "");
            String itemType = txnTypeMap.get(rs.getString("itemType"));

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
                if (isDebit)
                    txnAmount = txnAmount.negate();
            } catch (NumberFormatException nfe) {
                return null;
            }

            CashFlowInstance instance;
            Entity company = account.getCompany();
            CashFlowSource cashFlowSource = context.getById(Account.class, description);
            if (isDebit) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(account.getName()));
                    return null;
                }
                if (cashFlowSource == null)
                    cashFlowSource = spending;
                instance = new PaymentInstance(cashFlowSource, account, category,
                        accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO, company);
            } else if (!isDebit && category.equals("Paycheck")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(account.getName()));
                    return null;
                }
                Job job = getJobFromDescription(context, description);
                instance = new PaycheckInstance(account, job, category, accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO);
            } else if (!isDebit && category.equals("Reimbursement")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(account.getName()));
                    return null;
                }
                Job job = getJobFromDescription(context, description);
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
