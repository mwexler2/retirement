package name.wexler.retirement;

import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MintAccountReader extends AccountReader {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");
    public static final String mintPseudoCompany = "mint";

    @Override
    protected Account determineAccountFromFirstLine(Context context, BufferedReader br) {
        return null;
    }


    @Override
    protected AccountAndCashFlowInstance getInstanceFromLine(
            Context context,
            Account accountForStream,   // Not used as the account is specified in each transaction
            Map<String, String> line) throws AccountNotFoundException {
        if (!line.containsKey("Date") || !line.containsKey("Amount"))
            return null;
        String dateStr = line.get("Date");
        LocalDate txnDate = LocalDate.parse(dateStr, dateFormat);
        LocalDate accrualEnd = txnDate;
        BigDecimal txnAmount = BigDecimal.ZERO;
        String action = line.get("Transaction Type");

        String description = line.get("Description");
        String category = line.get("Category");
        String notes = line.get("Notes");
        String labelsStr = line.get("Labels");
        Account account = getAccountFromLine(context, line);
        List<String> labels = Arrays.asList(labelsStr.split(","));
        try {
            String amountStr = line.get("Amount");
            txnAmount = amountStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(amountStr);
            if (action.equals("debit"))
                txnAmount = txnAmount.negate();
        } catch (NumberFormatException nfe) {
            return null;
        }
        CashFlowInstance instance;
        instance = new CashFlowInstance(account.getCashFlowSource(), accrualEnd, accrualEnd, txnDate, txnAmount,
                BigDecimal.ZERO);

        instance.setAction(action);
        instance.setDescription(description);
        instance.setCategory(category);
        instance.setNotes(notes);
        instance.setLabels(labels);
        return new AccountAndCashFlowInstance(account, instance);
    }

    private Account getAccountFromLine(Context context, Map<String, String> line) throws AccountNotFoundException {
        String accountName = line.get("Account Name").trim();
        Account account = context.getById(Account.class, accountName);
        if (account == null)
            throw new AccountReader.AccountNotFoundException(accountName);
        return account;
    }
}

