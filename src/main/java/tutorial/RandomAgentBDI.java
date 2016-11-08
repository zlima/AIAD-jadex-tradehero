package tutorial;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.*;
import tutorial.Services.AgentRequestService;
import tutorial.Services.MarketAgentService;
import yahoofinance.histquotes.HistoricalQuote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cenas on 10/19/2016.
 */

@Agent
@Service
@ProvidedServices(@ProvidedService(type=MarketAgentService.class))
@Description("random agent")
public class RandomAgentBDI implements MarketAgentService {

    private double money; //dinheiro do agent
    private double winrate;
    private Map<String,Integer> stocksOwned;
    private Map<String,Map<Integer,Double>>stockHist;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Belief
    private boolean updatedstock;

    protected ArrayList<Stock> currentStockValues;

    @Agent
    IInternalAccess agent;



    @AgentCreated
    private void init(){
        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        winrate = 0.0;
        money = 0;
        currentStockValues = new ArrayList<Stock>();
        updatedstock = false;
    }

    @AgentBody
    private void body(){

    }

    private void parsetoStock(ArrayList<HashMap> quote){
        Stock tmpStock;
        currentStockValues.clear();
        for(int i=0;i<quote.size();i++){
            tmpStock = new Stock((String)quote.get(i).get("Symbol"),(Double)quote.get(i).get("Open"),(Double)quote.get(i).get("Close"),(Double)quote.get(i).get("High"),
                    (Double)quote.get(i).get("Low"),(Integer) quote.get(i).get("Volume"));
            currentStockValues.add(tmpStock);
        }

        updatedstock = !updatedstock;
    }


    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {
        parsetoStock(quote);
        //System.out.println(quote.get(0).getOpen());
        return null;
    }

    public IFuture<Void> ConfirmStockBuy(IComponentIdentifier agentid, String stockname, int quantity, double price) {

        if(agentid == this.agent.getComponentIdentifier()) { //confirmaçao do mercado
            if(money >= quantity*price){
                money -= quantity*price;
                System.out.println(money);
                Map<Integer,Double> tmpstock = new HashMap<Integer, Double>();
                tmpstock.put(quantity,price);
                stockHist.put(stockname,tmpstock);
                //guardar a stock
                if(stocksOwned.get(stockname)!= null){
                    stocksOwned.put(stockname,stocksOwned.get(stockname) + quantity);
                }else{
                    stocksOwned.put(stockname,quantity);
                }

            }else{
                //nao tem guito para comprar
                System.out.println("fodeu");
            }

        }
        return null;
    }

    @Plan(trigger=@Trigger(factchangeds="updatedstock"))
    public void newStockvalues(){
        buyStock("INTC",30,currentStockValues.get(currentStockValues.size()-1).getOpen());
    }

    private void buyStock(final String name,final int shares, final double price){

            SServiceProvider.getService(agent, AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                    .addResultListener(new DefaultResultListener<AgentRequestService>() {
                        public void resultAvailable(AgentRequestService service) {
                            service.BuyStocksRequest(agent.getComponentIdentifier(),name,shares,price);
                        }
                    });
        }


}
