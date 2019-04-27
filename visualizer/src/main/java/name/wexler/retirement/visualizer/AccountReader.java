package name.wexler.retirement.visualizer;

import com.opencsv.CSVReaderHeaderAware;
import name.wexler.retirement.visualizer.Asset.Account;
import name.wexler.retirement.visualizer.CashFlowInstance.CashFlowInstance;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AccountReader {
    public class AccountNotFoundException extends Exception {
        private final String accountName;

        public AccountNotFoundException(String accountName) {
            super("Account " + accountName + " not found");
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

    public static AccountReader factory(String companyId) throws ClassNotFoundException {
        if (companyId.equals("schwab")) {
            return new SchwabAccountReader();
        } else if (companyId.equals("mint")) {
            return new MintAccountReader();
        }
        throw new ClassNotFoundException("Can't find reader for " + companyId);
    }

    public void readCashFlowInstances(Context context, String companyId) throws IOException {
        Path historyDir = context.getHistoryDir(companyId);
        Path filePath = null;
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(historyDir)) {
            for (Path entry : dirStream) {
                filePath = historyDir.resolve(entry.getFileName());
                if (!filePath.toString().toLowerCase().endsWith(".csv")) {
                    continue;
                }
                System.out.println("entry filename = " + filePath);
                BufferedReader br = Files.newBufferedReader(filePath, Charset.defaultCharset());
                Account accountForStream = determineAccountFromFirstLine(context, br);
                Map<Account, List<CashFlowInstance>> cashFlowInstancesByAccount =
                        readCashFlowInstancesFromStream(context, accountForStream, br);
                cashFlowInstancesByAccount.forEach((account, instances) -> account.addCashFlowInstances(instances));
            }
        } catch (IOException | AccountNotFoundException ioe) {
            throw new IOException("Error reading " + filePath, ioe);
        }
    }

    private Map<Account, List<CashFlowInstance>> readCashFlowInstancesFromStream(
            Context context,
            Account accountForStream,
            BufferedReader br)
            throws IOException, AccountNotFoundException {
        Map<Account, List<CashFlowInstance>> cashFlowInstancesByAccount = new HashMap<>();

        CSVReaderHeaderAware reader = new CSVReaderHeaderAware(br);
        Map<String, String> line;
        while ((line = reader.readMap()) != null) {
            AccountAndCashFlowInstance instance = getInstanceFromLine(context, accountForStream, line);
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
        return cashFlowInstancesByAccount;
    }


    protected abstract Account determineAccountFromFirstLine(Context context, BufferedReader br);

    protected abstract AccountAndCashFlowInstance getInstanceFromLine(Context context,
                                                            Account account,
                                                            Map<String, String> line)
            throws AccountNotFoundException;
}
