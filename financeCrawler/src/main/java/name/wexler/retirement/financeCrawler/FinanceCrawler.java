package name.wexler.retirement.financeCrawler;

import name.wexler.retirement.datastore.*;

/**
 * Hello world!
 *
 */
public class FinanceCrawler
{
    public static void main( String[] args ) {
        DataStore ds = new DataStore();
        TickerHistory tickerHistory = ds.getTickerHistory();
        TxnHistory txnHistory = ds.getTxnHistory();
        Budgets budgets = ds.getBudgets();
        AccountTable accountTable = ds.getAccountTable();
        PositionHistory positionHistory = ds.getPositionHistory();

        if (false) {
            tickerHistory.getTickers().forEach((ticker, lastDate) -> {
                System.out.println("Crawling " + ticker + " for post " + lastDate);
                YahooFinanceCrawler yahooFinanceCrawler = new YahooFinanceCrawler(tickerHistory, ticker, lastDate);
                yahooFinanceCrawler.crawl();
            });
        }
        // CitibankCrawler citiCrawler = new CitibankCrawler()
        if (true) {
            MintCrawler mintCrawler = new MintCrawler(txnHistory, accountTable, budgets, txnHistory.getLastDate());
            mintCrawler.crawl();
        }
        if (false) {
            OFXCrawler ofxCrawler = new OFXCrawler(positionHistory, accountTable, txnHistory);
            ofxCrawler.crawl();
        }
    }
}
