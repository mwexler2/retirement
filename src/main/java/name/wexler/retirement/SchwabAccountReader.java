package name.wexler.retirement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.CSVParser;
import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.Asset.Account;
import name.wexler.retirement.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.CashFlowFrequency.ShareBalance;

import java.io.*;
import java.math.BigDecimal;
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
    @Override
    public List<CashFlowInstance> readCashFlowInstances(Account account) {
        List<CashFlowInstance> instances = new ArrayList<>();

        Path historyDir = account.getContext().getHistoryDir(account.getCompany());
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(historyDir)) {
            for (Path entry: dirStream) {
                List<CashFlowInstance> moreInstances = readCashFlowInstancesFromStream(account, entry.toFile());
                instances.addAll(moreInstances);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        return instances;
    }

    private List<CashFlowInstance> readCashFlowInstancesFromStream(Account account, File file) {
        List<CashFlowInstance> instances = new ArrayList<>();

        CSVReaderHeaderAware reader = null;
        try {
            reader = new CSVReaderHeaderAware(
                    new FileReader(file), 1, new CSVParser(),
                    false, false, 1, null);
            Map<String, String> line;
            while ((line = reader.readMap()) != null) {
                if (!line.containsKey("Date") || !line.containsKey("Price"))
                    continue;
                LocalDate date = LocalDate.parse(line.get("Date"), DateTimeFormatter.ISO_DATE);
                BigDecimal price = BigDecimal.ZERO;
                BigDecimal quantity = BigDecimal.ZERO;
                String security;
                try {
                    price = new BigDecimal(line.get("Close"));
                    quantity = new BigDecimal(line.get("Quantity"));
                    security = line.get("Symbol");
                } catch (NumberFormatException nfe) {
                    continue;   // skip invalid data (e.g. "1981-08-10,null,null,null,null,null,null")
                }
                ShareBalance balance = new ShareBalance(account.getContext(), date, quantity, price, security);
                CashFlowInstance instance = new SecurityTransaction(account.getContext(), account.getId(), price, balance);
                instances.add(instance);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instances;
    }
}

