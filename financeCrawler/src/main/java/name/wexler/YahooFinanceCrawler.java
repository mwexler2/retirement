package name.wexler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooFinanceCrawler extends SiteCrawler {
    private final String yahooFinanceBaseURL = "https://finance.yahoo.com/";
    private final String yahooQueryBaseURL = "https://query1.finance.yahoo.com/v7/finance/download/";
    private final int minSleepMilliseconds = 2000;
    private final int sleepMillisecondsBound = 3000;
    private Random random = new Random();

    YahooFinanceCrawler(List<String> tickers) {
        super();
        for (String ticker : tickers) {
            crawlURL(tickerToURL(ticker));
        }
    }

    @Override
    public ExecutorService getExecutorService() {
        return Executors.newSingleThreadExecutor();
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

    private String _getCSVFileName(String ticker) {
        String userHome = System.getProperty("user.home");
        String resourceDir = userHome + "/.retirement/history";
        String fileName = resourceDir + "/"  + ticker + ".csv";

        return fileName;
    }

    private void processDownloadFile(URL url, String content, Map<String, List<String>> headers) {
        Matcher tickerMatcher = tickerPattern.matcher(content);
        if (tickerMatcher.find()) {
            String ticker = tickerMatcher.group(1);
        try {
            String fileName = _getCSVFileName(ticker);
            PrintWriter out = new PrintWriter(fileName));
            out.print(content);
            out.close();
        } catch (IOException ioe) {
            throw new RuntimeException("can't write " + fileName, ioe)
        }
    }

    private void processHistoryPage(URL url, String content, Map<String, List<String>> headers) {
        Matcher crumbMatcher = crumbPattern.matcher(content);
        if (crumbMatcher.find()) {
            String crumb = crumbMatcher.group(1);
            Matcher tickerMatcher = tickerPattern.matcher(content);
            if (tickerMatcher.find()) {
                String ticker = tickerMatcher.group(1);
                long periodStart = 1544321863;
                long periodEnd = 1547000263;
                String interval = "1d";
                String event = "history";
                // https://query1.finance.yahoo.com/v7/finance/download/TWST?period1=1544321863&period2=1547000263&interval=1d&events=history&crumb=p1nTbKrcUvc
                URL downloadURL = getDownloadURL(ticker, periodStart, periodEnd, interval, event, crumb);
                crawlURL(downloadURL);
            }
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
