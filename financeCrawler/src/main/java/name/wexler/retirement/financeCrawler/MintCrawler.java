package name.wexler.retirement.financeCrawler;

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

    MintCrawler(TxnHistory txnHistory, LocalDate lastDate) {
        super();
        this.txnHistory = txnHistory;
        this.periodStart = lastDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }

    private String _getMintResourceFilename() {
        String userHome = System.getProperty("user.home");
        return userHome + "/.retirement/resources/mint.json";
    }

    private JSONArray getMintInfo() {
        JSONParser parser = new JSONParser();
        JSONArray result = null;

        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(_getMintResourceFilename())));
            result = getMintInfo(jsonString);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return result;
    }

    private JSONArray getMintInfo(String mintJSON) {
        JSONParser parser = new JSONParser();
        JSONArray result = null;

        try {
            result = (JSONArray) parser.parse(mintJSON);
        } catch (ParseException pe) {
            System.err.println(pe.getMessage());
        }
        return result;
    }


    public void crawl() {
        String line;
        String cmd = "/usr/local/bin/mintapi --keyring --headless --t mike.wexler@gmail.com --no_wait_for_sync --extended-transactions --include-investment";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String result = input.lines().collect(Collectors.joining(System.lineSeparator()));
                JSONArray txnList = getMintInfo(result);
                txnHistory.deleteAllRows();
                if (txnList != null)
                 processTxnListJSON(txnList, cmd);
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }


    private void processTxnListJSON(JSONArray txnList, String cmd) {
        System.out.println("Processing download file for '" + cmd + "'");
        txnList.forEach((txn) -> {
            System.out.println("txn = " + txn);
            Map<String, Object> fieldNameVals = new HashMap<>();
            // date, description, original_description, amount, txn_type, category, account_name, labels, notes
            fieldNameVals.put("date", ((JSONObject) txn).get("date").toString());
            fieldNameVals.put("description", ((JSONObject) txn).get("description"));
            fieldNameVals.put("original_description", ((JSONObject) txn).get("original_description"));
            fieldNameVals.put("amount", ((JSONObject) txn).get("amount"));
            fieldNameVals.put("txn_type", ((JSONObject) txn).get("transaction_type"));
            fieldNameVals.put("category", (String) ((JSONObject) txn).get("category"));
            fieldNameVals.put("account_name",  ((JSONObject) txn).get("account_name"));
            fieldNameVals.put("labels", ((JSONObject) txn).getOrDefault("labels", ""));
            fieldNameVals.put("notes", ((JSONObject) txn).getOrDefault("notes", ""));
            txnHistory.insertRow(fieldNameVals);
        });
    }

    private void processTransactions(String content, String url) {
        System.out.println("Processing download file for '" + url + "'");
        String[] lines = content.split("\n");
        String[] fieldNames = null;
        for (int i = 0; i < lines.length; ++i) {
            String[] fields = lines[i].split(",");
            if (i == 0)
                fieldNames = fields;
            else {
                Map<String, Object> fieldNameVals = new HashMap<>();
                for (int j = 0; j < fields.length; ++j) {
                    if (j < fieldNames.length) {
                        fieldNameVals.put(fieldNames[j], fields[j]);
                    }
                }
                txnHistory.insertRow(fieldNameVals);
            }
        }
    }

    private URL getDownloadURL(long periodStart, long periodEnd, String interval, String event, String crumb) {
        try {
            URL base = new URL(yahooQueryBaseURL);
            return new URL(base,
                    "?period1=" + periodStart +
                    "&period2=" + periodEnd +
                    "&interval=" + interval +
                    "&event=" + event +
                    "&crumb=" + crumb);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("can't create URL for " +
                    String.join(", " + Arrays.asList(periodStart, periodEnd, interval, crumb)));
        }
    }
}
