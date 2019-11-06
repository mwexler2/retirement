package name.wexler.retirement.visualizer;

import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashBalance;
import name.wexler.retirement.visualizer.CashFlowInstance.*;
import name.wexler.retirement.visualizer.CashFlowSource.CreditCardAccount;
import name.wexler.retirement.visualizer.CashFlowSource.Liability;
import name.wexler.retirement.visualizer.CashFlowSource.SecuredLoan;
import name.wexler.retirement.visualizer.Entity.Entity;
import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class AccountReader {
    public AccountReader() {

    }
    public class AccountNotFoundException extends Exception {
        private final String accountName;

        public AccountNotFoundException(String accountName) {
            super("AssetAccount " + accountName + " not found");
            this.accountName = accountName;
        }
    }

    protected class AccountAndCashFlowInstance {
        private final Account account;
        private final CashFlowInstance cashFlowInstance;

        AccountAndCashFlowInstance(Account account, CashFlowInstance cashFlowInstance) {
            this.account = account;
            this.cashFlowInstance = cashFlowInstance;
        }
    }

    public void readCashFlowInstances(Context context) throws IOException {
        DataStore ds = Retirement.getDataStore();
        try (ResultSet rs = ds.getTxnHistory().getTransactions()) {
            Map<Account, List<CashFlowInstance>> cashFlowInstancesByAccount =
                    readCashFlowInstancesFromResultSet(context, rs);
            cashFlowInstancesByAccount.forEach((account, instances) -> account.addCashFlowInstances(instances));
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }

    private Map<Account, List<CashFlowInstance>> readCashFlowInstancesFromResultSet (
            Context context,
            ResultSet rs) {
        Map<Account, List<CashFlowInstance>> cashFlowInstancesByAccount = new HashMap<>();

        try {
            while (rs.next()) {
                AccountAndCashFlowInstance instance = getInstanceFromResultSet(context, rs);
                if (instance == null)
                    continue;
                Account accountForInstance = instance.account;
                List<CashFlowInstance> cashFlowInstancesForAccount = cashFlowInstancesByAccount.get(accountForInstance);
                if (cashFlowInstancesForAccount == null) {
                    cashFlowInstancesForAccount = new ArrayList<>();
                    cashFlowInstancesByAccount.put(accountForInstance, cashFlowInstancesForAccount);
                }
                cashFlowInstancesForAccount.add(instance.cashFlowInstance);
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return cashFlowInstancesByAccount;
    }

    protected AccountAndCashFlowInstance getInstanceFromResultSet(
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
            String notes = rs.getString("notes");
            String labelsStr = ObjectUtils.defaultIfNull(rs.getString("labels"), "");
            List<String> labels = Arrays.asList(labelsStr.split(","));

            Account account = getAccountFromResultSet(context, rs, txnDate);
            if (account == null)
                return null;

            try {
                txnAmount = rs.getBigDecimal("amount");
                if (action.equals("debit"))
                    txnAmount = txnAmount.negate();
            } catch (NumberFormatException nfe) {
                return null;
            }

            CashFlowInstance instance;
            Entity company = account.getCompany();
            if (action.equals("debit")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(description));
                    return null;
                }
                instance = new PaymentInstance(account.getCashFlowSource(), accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO, company, category);
            } else if (action.equals("credit") && category.equals("Paycheck")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(description));
                    return null;
                }
                instance = new PaycheckInstance(account.getCashFlowSource(), accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO, company);
            } else if (action.equals("credit") && category.equals("Reimbursement")) {
                if (company == null) {
                    System.err.println(new AccountNotFoundException(description));
                    return null;
                }
                instance = new ReimbursementInstance(account.getCashFlowSource(), accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO, company);
            } else {
                instance = new CashFlowInstance(account.getCashFlowSource(),
                        accrualEnd, accrualEnd, txnDate, txnAmount,
                        BigDecimal.ZERO);
            }

            instance.setAction(action);
            instance.setDescription(description);
            instance.setCategory(category);
            instance.setNotes(notes);
            instance.setLabels(labels);
            return new AccountAndCashFlowInstance(account, instance);
        } catch (SQLException sqle) {
            System.err.println(sqle);
        } catch (AccountNotFoundException anfe) {
            System.err.println(anfe);
        }
        return null;
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
                try {
                    assetAccount = new AssetAccount(context, accountName, new ArrayList<>(0), new CashBalance(txnDate, BigDecimal.ZERO), new ArrayList<>(0), accountName, accountName,
                            Arrays.asList(accountName));
                } catch (Entity.DuplicateEntityException dee) {
                    System.err.println(accountName + ":" + dee);
                    return null;
                } catch (AssetAccount.CashFlowSourceNotFoundException cfsnfe) {
                    System.err.println(accountName + ":" + cfsnfe);
                    return null;
                }
            }
            account = assetAccount;
        }
        return account;
    }
}
