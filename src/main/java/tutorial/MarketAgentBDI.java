package tutorial;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.*;
import tutorial.Services.AgentRequestService;
import tutorial.Services.MarketAgentService;
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
@ProvidedServices(@ProvidedService(type=AgentRequestService.class))
@Description("Market.")
public class MarketAgentBDI implements AgentRequestService {

    private Map<String, Stock> stocks;

    private Map<String,List<HistoricalQuote>> stockHist;
    private MarketAgentService serviço;


    private int days;
    private boolean openstatus;

    @Belief
    private int dayspassed;
    @Belief
    private String[] symbols = new String[] {"INTC"/*, "BABA", "TSLA", "YHOO", "GOOG"*/};

    @Belief
    private  Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=3000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    IInternalAccess agent;


    @AgentCreated
    private void init(){
        dayspassed = 0;
        openstatus = true;
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
            Market.get(symbols[i]).add(stockHist.get(symbols[i]).get(days));
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
            Market.put(symbols[i], new ArrayList());
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
        SServiceProvider.getService(agent, MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<MarketAgentService>() {
                    public void resultAvailable(MarketAgentService service) {
                        ArrayList<HashMap> sendMarketVal = new ArrayList<HashMap>();
                        createLastQuoteHash(sendMarketVal);
                        sendLastMarketValues(service,sendMarketVal);
                    }
                });

        return null;

    }

    public void createLastQuoteHash(ArrayList<HashMap> quote){
        HashMap temp = new HashMap();

        for(int i=0; i<symbols.length;i++){
            temp.put("Symbol",symbols[i]);
            temp.put("Open",Market.get(symbols[i]).get(dayspassed-1).getOpen().doubleValue());
            temp.put("Close",Market.get(symbols[i]).get(dayspassed-1).getClose().doubleValue());
            temp.put("High",Market.get(symbols[i]).get(dayspassed-1).getHigh().doubleValue());
            temp.put("Low",Market.get(symbols[i]).get(dayspassed-1).getLow().doubleValue());
            temp.put("Volume",Market.get(symbols[i]).get(dayspassed-1).getVolume().intValue());
            quote.add(temp);
        }
    }

    public void sendLastMarketValues(MarketAgentService service, ArrayList<HashMap> cenas) {
        service.UpdateMarketService(cenas);
    }

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price){
        SServiceProvider.getService(agent, MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<MarketAgentService>() {
                    public void resultAvailable(MarketAgentService service) {
                        service.ConfirmStockBuy(agentid,stockname,quantity,price);
                    }
                });
        return null;
    }


    public IFuture<Void> BuyStocksRequest(IComponentIdentifier agentid, String stockname, int quantity, double price) {
        System.out.println("Cenas 2: "+Market.get(stockname).get(dayspassed-1).getOpen().doubleValue());
        if(Market.get(stockname).get(dayspassed-1).getOpen().doubleValue() == price)
        ConfirmStockBuy(agentid,stockname,quantity,price);
        return null;
    }
}
