package name.wexler.retirement.financeCrawler;

import name.wexler.retirement.datastore.AccountTable;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import name.wexler.retirement.datastore.TxnHistory;
import name.wexler.retirement.datastore.Budgets;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MintCrawler {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final boolean writeHeader = false;
    private final TxnHistory txnHistory;
    private final Budgets budgets;
    private final AccountTable accountTable;
    private static final String mintSource = "mint";

    MintCrawler(TxnHistory txnHistory, AccountTable accountTable, Budgets budgets, LocalDate lastDate) {
        super();
        this.txnHistory = txnHistory;
        this.budgets = budgets;
        this.accountTable = accountTable;
    }

    private JSONArray getMintInfoArray(String mintJSON) {
        JSONParser parser = new JSONParser();
        JSONArray result = null;

        try {
            result = (JSONArray) parser.parse(mintJSON);
        } catch (ParseException pe) {
            throw new RuntimeException("getMintInfoArray", pe);
        }
        return result;
    }

    private JSONObject getMintInfoObject(String mintJSON) {
        JSONParser parser = new JSONParser();
        JSONObject result = null;

        try {
            result = (JSONObject) parser.parse(mintJSON);
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
        return result;
    }


    public void crawl() {
        crawlAccounts();
        crawlTransactions();
        crawlBudgets();
    }


    private void crawlTransactions() {
        String line;
        String cmd = "/usr/local/bin/mintapi --keyring mike.wexler@gmail.com --headless --no_wait_for_sync --extended-transactions --include-investment";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            String result = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                result = input.lines().collect(Collectors.joining(System.lineSeparator()));
                JSONArray txnList = getMintInfoArray(result);
                if (txnList != null)
                    processTxnListJSON(txnList, cmd);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void crawlBudgets() {
        String cmd = "/usr/local/bin/mintapi --keyring mike.wexler@gmail.com --headless --no_wait_for_sync --budgets";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            String result = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                result = input.lines().collect(Collectors.joining(System.lineSeparator()));
                JSONObject budgets = getMintInfoObject(result);
                if (budgets != null)
                    processBudgetsJSON(budgets, cmd);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


    private  void crawlAccounts() {
        String line;
        String cmd = "/usr/local/bin/mintapi --keyring mike.wexler@gmail.com --headless --no_wait_for_sync --extended-accounts --include-investment";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            String result = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                result = input.lines().collect(Collectors.joining(System.lineSeparator()));
                JSONArray txnList = getMintInfoArray(result);
                if (accountTable != null)
                    processAccountListJSON(txnList, cmd);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }


    private final static String txnTypeMap[] =
            {
                    "debit",
                    "transfer",
                    "credit"
            };
    private void processTxnListJSON(JSONArray txnList, String cmd) {
        System.out.println("Processing download file for '" + cmd + "'");
        txnList.forEach((txn) -> {
            Map<String, Object> fieldNameVals = new HashMap<>();
            for (Object key: ((JSONObject) txn).keySet()) {
                Object value =  ((JSONObject) txn).getOrDefault(key, "");
                if (((String) key).equals("txnType")) {
                    value = txnTypeMap[((Long) value).intValue()];
                }
                fieldNameVals.put((String) key, value);
            }
            fieldNameVals.put(TxnHistory.txnId, fieldNameVals.get("id").toString());
            fieldNameVals.put(TxnHistory.source, mintSource);
            txnHistory.insertRow(fieldNameVals);
        });
    }

    private void processBudgetsJSON(JSONObject budgetEntries, String cmd) {
        System.out.println("Processing download file for '" + cmd + "'");
        budgetEntries.forEach((grouping, entries) -> {
            ((JSONArray) entries).forEach((budgetEntry) -> {
                Map<String, Object> fieldNameVals = new HashMap<>();
                for (Object key : ((JSONObject) budgetEntry).keySet()) {
                    Object value = ((JSONObject) budgetEntry).getOrDefault(key, "");
                    fieldNameVals.put((String) key, value);
                }
                if (!fieldNameVals.containsKey("catName")) {
                    fieldNameVals.put("catName", fieldNameVals.get("cat")); // If no category name is specified use category id
                }
                fieldNameVals.put(TxnHistory.source, mintSource);
                budgets.insertRow((String) grouping, fieldNameVals);
            });
        });
    }


    private void processAccountListJSON(JSONArray accountList, String cmd) {
        System.out.println("Processing download file for '" + cmd + "'");
        accountList.forEach((account) -> {
            System.out.println("account = " + account);
            Map<String, Object> fieldNameVals = new HashMap<>();
            for (Object key: ((JSONObject) account).keySet()) {
                Object value =  ((JSONObject) account).getOrDefault(key, "");
                fieldNameVals.put((String) key, value);
            }
            accountTable.insertRow(fieldNameVals);
        });
    }
}
