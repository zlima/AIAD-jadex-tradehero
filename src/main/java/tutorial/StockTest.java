package tutorial;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Cenas on 10/13/2016.
 */
public class StockTest {

    public StockTest() throws IOException {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1); // from 1 year ago

        Stock google = YahooFinance.get("GOOG");
       // List<HistoricalQuote> googleHistQuotes = google.getHistory(from, to, Interval.DAILY);

        System.out.println(google.getQuote().getChangeInPercent());
    }

}
