package name.wexler.retirement.financeCrawler;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.util.JSONPObject;
import name.wexler.retirement.datastore.AccountTable;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import name.wexler.retirement.datastore.TxnHistory;
import org.json.simple.JSONObject;
import name.wexler.retirement.datastore.TickerHistory;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MintCrawler {
    private static final String loginURL = "https://accounts.intuit.com/access_client/sign_in";
    private static final String yahooFinanceBaseURL = "https://finance.yahoo.com/";
    private static final String yahooQueryBaseURL = "https://query1.finance.yahoo.com/v7/finance/download/";
    private static final int minSleepMilliseconds = 2000;
    private static final int sleepMillisecondsBound = 3000;
    private long periodStart;
    private final long periodEnd = (System.currentTimeMillis() / 1000L);
    private Random random = new Random();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean writeHeader = false;
    private TxnHistory txnHistory;
    private AccountTable accountTable;

    MintCrawler(TxnHistory txnHistory, AccountTable accountTable, LocalDate lastDate) {
        super();
        this.txnHistory = txnHistory;
        this.accountTable = accountTable;
        this.periodStart = lastDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private JSONArray getMintInfo(String mintJSON) {
        JSONParser parser = new JSONParser();
        JSONArray result = null;

        try {
            result = (JSONArray) parser.parse(mintJSON);
        } catch (ParseException pe) {
            System.err.println(pe);
        }
        return result;
    }


    public void crawl() {
        crawlAccounts();
        crawlTransactions();
    }


    private void crawlTransactions() {
        String line;
        String cmd = "/usr/local/bin/mintapi --keyring mike.wexler@gmail.com --headless --no_wait_for_sync --extended-transactions --include-investment";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            String result = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                result = input.lines().collect(Collectors.joining(System.lineSeparator()));
                JSONArray txnList = getMintInfo(result);
                txnHistory.deleteAllRows();
                if (txnList != null)
                    processTxnListJSON(txnList, cmd);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
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
                JSONArray txnList = getMintInfo(result);
                accountTable.deleteAllRows();
                if (accountTable != null)
                    processAccountListJSON(txnList, cmd);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }


    private void processTxnListJSON(JSONArray txnList, String cmd) {
        System.out.println("Processing download file for '" + cmd + "'");
        txnList.forEach((txn) -> {
            Map<String, Object> fieldNameVals = new HashMap<>();
            for (Object key: ((JSONObject) txn).keySet()) {
                System.out.println("key = " + key + ", value = " + ((JSONObject) txn).getOrDefault(key, ""));
                fieldNameVals.put((String) key, ((JSONObject) txn).getOrDefault(key, ""));
            }
            txnHistory.insertRow(fieldNameVals);
        });
    }

    static Map<String, String> txnTypeMap = Map.ofEntries(
            Map.entry("0", "debit"),
            Map.entry("1", "transfer"),
            Map.entry("2", "credit"));
    private void processAccountListJSON(JSONArray accountList, String cmd) {
        System.out.println("Processing download file for '" + cmd + "'");
        accountList.forEach((account) -> {
            System.out.println("account = " + account);
            Map<String, Object> fieldNameVals = new HashMap<>();
            for (Object key: ((JSONObject) account).keySet()) {
                Object value =  ((JSONObject) account).getOrDefault(key, "");
                System.out.println("key = " + key + ", value = " + value);
                if (((String) key).equals("txn_type")) {
                    value = txnTypeMap.get(value);
                }
                fieldNameVals.put((String) key, value);
            }
            accountTable.insertRow(fieldNameVals);
        });
    }
}
