package name.wexler.retirement.financeCrawler;

import name.wexler.retirement.datastore.TickerHistory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooFinanceCrawler extends SiteCrawler {
    private static final String yahooFinanceBaseURL = "https://finance.yahoo.com/";
    private static final String yahooQueryBaseURL = "https://query1.finance.yahoo.com/v7/finance/download/";
    private static final int minSleepMilliseconds = 2000;
    private static final int sleepMillisecondsBound = 3000;
    private long periodStart;
    private final long periodEnd = (System.currentTimeMillis() / 1000L);
    private Random random = new Random();
    private final String ticker;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean writeHeader = false;
    private TickerHistory tickerHistory;

    YahooFinanceCrawler(TickerHistory tickerHistory, String ticker, LocalDate lastDate) {
        super();
        this.tickerHistory = tickerHistory;
        this.ticker = ticker;
        this.periodStart = lastDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }


    public void crawl() {
        crawlURL(tickerToURL(ticker));
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void processResponse(URL url, String content, Map<String, List<String>> headers) {
        if (url.toString().contains("/history?p=")) {
            processHistoryPage(url, content, headers);
        } else if (url.toString().contains("/v7/finance/download/")) {
            processDownloadFile(url, content, headers);
        }
        try {
            TimeUnit.MILLISECONDS.sleep(minSleepMilliseconds + random.nextInt(sleepMillisecondsBound));
        } catch (InterruptedException ie) {
            // do nothing
        }
    }

    private URL tickerToURL(String ticker) {
        try {
            URL base = new URL(yahooFinanceBaseURL);
            URL historyURL = new URL(base, "/quote/" + ticker + "/history?p=" + ticker);
            return historyURL;
        } catch (MalformedURLException mue) {
            throw new RuntimeException("can't create URL for " + ticker, mue);
        }
    }

    static final Pattern crumbPattern = Pattern.compile("\"CrumbStore\":\\{\"crumb\":\"([^\"]*)\"\\}");
    static final Pattern tickerPattern = Pattern.compile("\"ticker\":\"([^\"]+)\"");
    static final Pattern tickerURLPattern = Pattern.compile("/download/([^?]+)\\?");



    private void processDownloadFile(URL url, String content, Map<String, List<String>> headers) {
        System.out.println("Processing download file for " + url);
        String[] lines = content.split("\n");
        String[] fieldNames = null;
        for (int i = 0; i < lines.length; ++i) {
            String[] fields = lines[i].split(",");
            if (i == 0)
                fieldNames = fields;
            else {
                Map<String, String> fieldNameVals = new HashMap<>();
                for (int j = 0; j < fields.length; ++j) {
                    if (j < fieldNames.length) {
                        fieldNameVals.put(fieldNames[j], fields[j]);
                    }
                }
                if (fieldNameVals.get("Open") == null)
                    continue;
                tickerHistory.insertRow(ticker, fieldNameVals);
            }
        }
    }

    private void processHistoryPage(URL url, String content, Map<String, List<String>> headers) {
        System.out.println("Processing history page for " + url);
        Matcher crumbMatcher = crumbPattern.matcher(content);
        if (crumbMatcher.find()) {
            String crumb = crumbMatcher.group(1);
            String interval = "1d";
            String event = "history";
            // https://query1.finance.yahoo.com/v7/finance/download/TWST?period1=1544321863&period2=1547000263&interval=1d&events=history&crumb=p1nTbKrcUvc
            URL downloadURL = getDownloadURL(ticker, periodStart, periodEnd, interval, event, crumb);
            crawlURL(downloadURL);
        }
    }


    private URL getDownloadURL(String ticker, long periodStart, long periodEnd, String interval, String event, String crumb) {
        try {
            URL base = new URL(yahooQueryBaseURL);
            URL downloadURL = new URL(base, ticker +
                    "?period1=" + periodStart +
                    "&period2=" + periodEnd +
                    "&interval=" + interval +
                    "&event=" + event +
                    "&crumb=" + crumb);
            return downloadURL;
        } catch (MalformedURLException mue) {
            throw new RuntimeException("can't create URL for " +
                    String.join(", " + Arrays.asList(ticker, periodStart, periodEnd, interval, crumb)));
        }
    }
}
