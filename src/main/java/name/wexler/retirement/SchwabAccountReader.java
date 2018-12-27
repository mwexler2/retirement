package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.CSVParser;
import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.CashFlowFrequency.ShareBalance;
import name.wexler.retirement.Entity.Company;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SchwabAccountReader extends AccountReader {
    private static final DateTimeFormatter schwabDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    @Override
    public void readCashFlowInstances(Context context, Company company) {

        Path historyDir = context.getHistoryDir(company);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(historyDir)) {
            for (Path entry: dirStream) {
                BufferedReader br = Files.newBufferedReader(historyDir.resolve(entry.getFileName()), Charset.defaultCharset());
                Account account = determineAccountFromFirstLine(context, br);
                List<CashFlowInstance> instances = readCashFlowInstancesFromStream(account, br);
                account.addCashFlowInstances(instances);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private Account determineAccountFromFirstLine(Context context, BufferedReader br) {
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

    private List<CashFlowInstance> readCashFlowInstancesFromStream(Account account, BufferedReader br) {
        List<CashFlowInstance> instances = new ArrayList<>();

        CSVReaderHeaderAware reader = null;
        Context context = account.getContext();
        try {
            reader = new CSVReaderHeaderAware(br);
            Map<String, String> line;
            while ((line = reader.readMap()) != null) {
                if (!line.containsKey("Date") || !line.containsKey("Price"))
                    continue;
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
                try {
                    String amountStr = line.get("Amount").replace("$", "");
                    txnAmount = amountStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(amountStr);
                    String priceStr = line.get("Price").replace("$", "");
                    sharePrice = priceStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceStr);
                } catch (NumberFormatException nfe) {
                    continue;   // skip invalid data (e.g. "1981-08-10,null,null,null,null,null,null")
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

                    ShareBalance balance = new ShareBalance(account.getContext(), txnDate, quantity, sharePrice, symbol);
                    instance = new SecurityTransaction(context, account, txnAmount, balance);
                }
                instances.add(instance);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instances;
    }
}

