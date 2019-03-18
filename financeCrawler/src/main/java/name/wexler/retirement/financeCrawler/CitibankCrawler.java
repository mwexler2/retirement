package name.wexler.retirement.financeCrawler;

import com.opencsv.CSVReaderHeaderAware;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CitibankCrawler extends SiteCrawler {
    private static final String citibankBaseURL = "https://online.citibank.com/";
    private static final String citibankLoginBaseURL = "https://online.citi.com/US/login.do";
    private static final String yahooQueryBaseURL = "https://query1.finance.yahoo.com/v7/finance/download/";
    private static final int minSleepMilliseconds = 2000;
    private static final int sleepMillisecondsBound = 3000;
    private long periodStart;
    private final long periodEnd = (System.currentTimeMillis() / 1000L);
    private static final long minPeriodStart = -2208960000L;
    private Random random = new Random();
    private final String ticker;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean writeHeader = false;

    CitibankCrawler(String ticker) {
        super();
        this.ticker = ticker;
        this.periodStart = getPeriodStart();
    }

    private long getPeriodStart() {
        long result = minPeriodStart;
        String csvFile = _getCSVFileName(ticker);

        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(csvFile))) {
            Map<String, String> line;
            while ((line = reader.readMap()) != null) {
                if (!line.containsKey("Date"))
                    continue;
                LocalDate date = LocalDate.parse(line.get("Date"), DateTimeFormatter.ISO_DATE);
                ZoneId zoneId = ZoneId.systemDefault();
                result = date.plus(1, ChronoUnit.DAYS).atStartOfDay(zoneId).toEpochSecond();
            }
        } catch (IOException e) {
            writeHeader = true;
            return minPeriodStart;
        }
        return result;
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
            URL base = new URL(citibankBaseURL);
            URL historyURL = new URL(base, "/quote/" + ticker + "/history?p=" + ticker);
            return historyURL;
        } catch (MalformedURLException mue) {
            throw new RuntimeException("can't create URL for " + ticker, mue);
        }
    }

    static final Pattern crumbPattern = Pattern.compile("\"CrumbStore\":\\{\"crumb\":\"([^\"]*)\"\\}");
    static final Pattern tickerPattern = Pattern.compile("\"ticker\":\"([^\"]+)\"");
    static final Pattern tickerURLPattern = Pattern.compile("/download/([^?]+)\\?");

    private String _getCSVFileName(String ticker) {
        String userHome = System.getProperty("user.home");
        String resourceDir = userHome + "/.retirement/history";
        String fileName = resourceDir + "/"  + ticker + ".csv";

        return fileName;
    }

    private void processDownloadFile(URL url, String content, Map<String, List<String>> headers) {
        String fileName = _getCSVFileName(ticker);
        if (!writeHeader) { // If we already have a head, skip the first line.
            content = content.substring(content.indexOf('\n') + 1);
        }
        try {

            PrintWriter out = new PrintWriter(new FileOutputStream(fileName, true));
            out.print(content);
            out.close();
        } catch (IOException ioe) {
            throw new RuntimeException("can't write " + fileName, ioe);
        }
    }

    private void processHistoryPage(URL url, String content, Map<String, List<String>> headers) {
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
