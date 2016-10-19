package tutorial;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.micro.MicroAgentFactory;
import jadex.micro.annotation.*;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.util.*;

/**
 * Created by Cenas on 10/13/2016.
 */
@Agent
@Description("Market.")
public class MarketAgentBDI {

    private Map<String, Stock> stocks;

    private Map<String,List<HistoricalQuote>> stockHist;

   private String[] symbols = new String[] {"INTC"/*, "BABA", "TSLA", "YHOO", "GOOG"*/};

    private int days;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;


    @AgentCreated
    private void init(){
        try {
            getStocksHist();
        } catch (IOException e) {
            e.printStackTrace();
        }
            initMarket();
        days =stockHist.get("INTC").size()-1;
    }

    @AgentBody
    private void body(){

        bdiFeature.adoptPlan("updateMarketPlan");

    }


    public void updateMarket(){

        if(days < 0)
            return; //chegou ao fim dos dias

        for(int i=0; i< symbols.length;i++){
            Market.get(symbols[i]).add(stockHist.get(symbols[i]).get(days));
        }

        days--;

    }

    @Plan(trigger=@Trigger(factchangeds="time"))
    protected void updateMarketPlan(){
        updateMarket();
        System.out.println(Market.get("INTC").size());
    }





    private void initMarket(){

        Market = new HashMap<String,List<HistoricalQuote>>();


        for(int i =0; i<symbols.length;i++) {
            Market.put(symbols[i], new ArrayList<HistoricalQuote>());
        }
    }


    private void getStocksHist() throws IOException {

        stockHist = new HashMap<String,List<HistoricalQuote>>();

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1); // from 1 year ago
        stocks = YahooFinance.get(symbols,true);


        for(int i=0;i<symbols.length;i++){
            stockHist.put(symbols[i],stocks.get(symbols[i]).getHistory(from, to, Interval.DAILY));
        }

        System.out.println(stockHist.get("INTC").get(0).getDate().getTime());
        System.out.println(stockHist.get("INTC").get(252).getDate().getTime());

        System.out.println(stockHist.get("INTC").get(0).getClose());
        System.out.println(stockHist.get("INTC").get(0).getHigh());
        System.out.println(stockHist.get("INTC").get(0).getOpen());


       /* for(int i=0;i<symbols.length;i++){
            System.out.println(stocks.get(symbols[i]).getSymbol());
        }
        */
    }


}
