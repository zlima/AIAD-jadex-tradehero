package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
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
@Description("Market.")
public class MarketAgentBDI {

    private Map<String, Stock> stocks;

    private Map<String,List<HistoricalQuote>> stockHist;
    private UpdateMarketService serviço;


    private int days;

    @Belief
    private int dayspassed;
    @Belief
    private String[] symbols = new String[] {"INTC"/*, "BABA", "TSLA", "YHOO", "GOOG"*/};

    @Belief
    private  Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    IInternalAccess agent;


    @AgentCreated
    private void init(){
        dayspassed = 0;
        try {
            getStocksHist();
        } catch (IOException e) {
            e.printStackTrace();
        }
            initMarket();
        days = stockHist.get("INTC").size()-1;
    }

    @AgentBody
    private void body(){

        bdiFeature.adoptPlan("updateMarketPlan");

    }


    public void updateMarket(){

        if(days < 0)
            return; //chegou ao fim dos dias

        for(int i=0; i< symbols.length;i++){
            Market.get(symbols[i]).add(stockHist.get(symbols[i]).get(days));;
        }

        days--;
        dayspassed++;

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



    @Plan(trigger=@Trigger(factchangeds="dayspassed"))
    public IFuture<Void> UpdateMarketService() {
        SServiceProvider.getService(agent, UpdateMarketService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<UpdateMarketService>() {
                    public void resultAvailable(UpdateMarketService service) {
                        ArrayList<HashMap> sendMarketVal = new ArrayList<HashMap>();
                        createLastQuoteHash(sendMarketVal);
                        sendLastMarketValues(service,sendMarketVal);
                    }
                });

        return null;

    }

    public void createLastQuoteHash(ArrayList<HashMap> quote){
        HashMap test = new HashMap();
        for(int i=0; i<symbols.length;i++){
            test.put("Symbol",symbols[i]);
            test.put("Open",Market.get(symbols[i]).get(dayspassed-1).getOpen().doubleValue());
            test.put("Close",Market.get(symbols[i]).get(dayspassed-1).getOpen().doubleValue());
            quote.add(test);
        }
    }

    public void sendLastMarketValues(UpdateMarketService service, ArrayList<HashMap> cenas) {
        service.UpdateMarketService(cenas);
    }

}
