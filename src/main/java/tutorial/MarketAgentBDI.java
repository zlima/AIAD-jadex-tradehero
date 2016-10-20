package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
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
@ProvidedServices(@ProvidedService(name="transser", type=UpdateMarketService.class,implementation=@Implementation(IBDIAgent.class)))
@Description("Market.")
public class MarketAgentBDI {

    private Map<String, Stock> stocks;

    private Map<String,List<HistoricalQuote>> stockHist;


    private int days;


    @Belief
    private String[] symbols = new String[] {"INTC"/*, "BABA", "TSLA", "YHOO", "GOOG"*/};

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

    }


    @Plan(trigger=@Trigger(service=@ServiceTrigger(type=UpdateMarketService.class)))
    public class UpdateMarketServ
    {

        @PlanBody
        public Map<String,HistoricalQuote> body()
        {
            Map<String,HistoricalQuote> updatedValue = new HashMap<String, HistoricalQuote>();

            for(int i=0;i<symbols.length;i++){
                updatedValue.put(symbols[i],Market.get(symbols[i]).get(days));
            }

            return updatedValue;
        }
    }



}
