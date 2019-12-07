package name.wexler.retirement.financeCrawler;

import name.wexler.retirement.datastore.AccountTable;
import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.datastore.TickerHistory;
import name.wexler.retirement.datastore.TxnHistory;

/**
 * Hello world!
 *
 */
public class FinanceCrawler
{
    public static void main( String[] args ) {
        DataStore ds = new DataStore();

        if (false) {
            TickerHistory tickerHistory = ds.getTickerHistory();
            tickerHistory.getTickers().forEach((ticker, lastDate) -> {
                System.out.println("Crawling " + ticker + " for post " + lastDate);
                YahooFinanceCrawler yahooFinanceCrawler = new YahooFinanceCrawler(tickerHistory, ticker, lastDate);
                yahooFinanceCrawler.crawl();
            });
        }
        // CitibankCrawler citiCrawler = new CitibankCrawler()
        if (true) {
            TxnHistory txnHistory = ds.getTxnHistory();
            AccountTable accountTable = ds.getAccountTable();
            MintCrawler mintCrawler = new MintCrawler(txnHistory, accountTable, txnHistory.getLastDate());
            mintCrawler.crawl();
        }
    }
}
