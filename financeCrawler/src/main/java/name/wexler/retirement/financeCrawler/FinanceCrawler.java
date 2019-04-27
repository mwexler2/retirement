package name.wexler.retirement.financeCrawler;

import java.util.Arrays;
import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.datastore.TickerHistory;

/**
 * Hello world!
 *
 */
public class FinanceCrawler
{
    public static void main( String[] args )
    {
        DataStore ds = new DataStore();

        TickerHistory tickerHistory = ds.getTickerHistory();
        tickerHistory.getTickers().forEach((ticker, lastDate) -> {
            System.out.println("Crawling " + ticker + " for post " + lastDate);
            YahooFinanceCrawler yahooFinanceCrawler = new YahooFinanceCrawler(tickerHistory, ticker, lastDate);
            yahooFinanceCrawler.crawl();
        });

    }
}
