package name.wexler.retirement;

import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.CashFlowFrequency.ShareBalance;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SchwabAccountReader extends AccountReader {
    private static final DateTimeFormatter schwabDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String usTreasuryPrefix = "US TREASURY";

    @Override
    protected Account determineAccountFromFirstLine(Context context, BufferedReader br) {
        try {
            String firstLine = br.readLine();
            String firstLineWithoutPrefix = firstLine.replace("\"Transactions  for account ", "");
            String[] parts = firstLineWithoutPrefix.split(" as of ");
            String accountIndicator = parts[0];
            return context.getById(Account.class, accountIndicator);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    protected AccountAndCashFlowInstance getInstanceFromLine(Context context,
                                                   Account account,
                                                   Map<String, String> line) {
        if (!line.containsKey("Date") || !line.containsKey("Price"))
            return null;
        String dateStr = line.get("Date");
        String[] dateParts = dateStr.split(" as of ");
        String accrualEndStr = dateParts[0];
        String txnDateStr = accrualEndStr;
        if (dateParts.length > 1) {
            txnDateStr = dateParts[1];
        }

        LocalDate txnDate = LocalDate.parse(txnDateStr, schwabDate);
        LocalDate accrualEnd = LocalDate.parse(accrualEndStr, schwabDate);
        BigDecimal sharePrice = BigDecimal.ZERO;
        BigDecimal txnAmount = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;
        String symbol = line.get("Symbol");
        String action = line.get("Action");
        String description = line.get("Description");
        String amountStr = line.get("Amount").replace("$", "").replace(",", "").trim();
        try {
            txnAmount = amountStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(amountStr);
        } catch (NumberFormatException nfe) {
            System.out.println("Can't parse amount: " + amountStr);
            return null;   // skip invalid data (e.g. "1981-08-10,null,null,null,null,null,null")
        }
        String priceStr = line.get("Price").replace("$", "").replace(",", "").trim();
        try {
            sharePrice = priceStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceStr);
            // For Whatever reason, US Treasuries are reported with a price that is 100 times too high

            if (description.startsWith(usTreasuryPrefix)) {
                sharePrice = sharePrice.divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP);
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Can't parse price" + priceStr);
            return null;   // skip invalid data (e.g. "1981-08-10,null,null,null,null,null,null")
        }

        CashFlowInstance instance;
        if (symbol.isEmpty()) {
            instance = new CashFlowInstance(account.getCashFlowSource(), accrualEnd, accrualEnd, txnDate, txnAmount,
                    BigDecimal.ZERO);

        } else {
            if (context.getById(Security.class, symbol) == null) {
                context.put(Security.class, symbol, new Security(context, symbol));
            }
            String quantityStr = line.get("Quantity");
            quantity = quantityStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(line.get("Quantity"));
            if (action.equals("Sell")) {
                quantity = quantity.negate();
            }

            ShareBalance balance = new ShareBalance(account.getContext(), txnDate, quantity, sharePrice, symbol);
            instance = new SecurityTransaction(context, account, txnAmount, balance);
        }
        instance.setAction(action);
        instance.setDescription(description);
        return new AccountAndCashFlowInstance(account, instance);
    }
}

