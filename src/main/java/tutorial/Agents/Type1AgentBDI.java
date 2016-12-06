package tutorial.Agents;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.*;
import org.apache.batik.bridge.Mark;
import org.apache.batik.gvt.Marker;
import tutorial.Services.AgentChatService;
import tutorial.Services.AgentRequestService;
import tutorial.Services.MarketAgentService;
import yahoofinance.histquotes.HistoricalQuote;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Cenas on 12/5/2016.
 */

@Agent
@Service
@ProvidedServices({
        @ProvidedService(type=MarketAgentService.class),
        @ProvidedService(type=AgentChatService.class)
})
@Description("agent type 1")
public class Type1AgentBDI implements MarketAgentService, AgentChatService  {

    private double money; //dinheiro do agent
    private double winrate;
    private Map<String,Integer> stocksOwned;
    private Map<String,Integer> stockHist;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Belief
    private boolean updatedstock;

    @Agent
    IInternalAccess agent;

    protected ArrayList<ArrayList<HashMap>> stockValues;

    protected HashMap<String,double[]> recordVariationMap;


    @AgentCreated
    private void init(){
        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        winrate = 0.0;
        money = 400000;
        stockValues = new ArrayList<ArrayList<HashMap>>();
        stockHist = new HashMap<String, Integer>();
        recordVariationMap = new HashMap<String, double[]>();
    }

    @AgentBody
    private void body(){



    }



    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {
        stockValues.add(quote);

        updatedstock = !updatedstock;

        return null;
    }


    //alterar aqui
    @Plan(trigger=@Trigger(factchangeds="updatedstock"))
    public void newStockvalues(){
        recordVariation();
        decisionFunc();
    }

    public void recordVariation() {

        if (recordVariationMap.size() == 0) {
            for (int i = 0; i < stockValues.get(stockValues.size() - 1).size(); i++) {
                HashMap<String, Integer[]> temp = new HashMap<String, Integer[]>();
                double[] tmp2 = new double[]{0, 0};
                recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(i).get("Symbol"), tmp2);
            }
        } else {

            for (int j = 0; j < stockValues.get(stockValues.size() - 1).size(); j++) {

                if (stockValues.get(stockValues.size() - 1).get(0).size() == 5) {

                    if ((Double) stockValues.get(stockValues.size() - 1).get(j).get("Close") > (Double) stockValues.get(stockValues.size() - 2).get(j).get("Open")) {

                        if (recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"))[0] >= 0.0) {

                            double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                            tmp3[0]++;
                            tmp3[1] += (Double) stockValues.get(stockValues.size() - 1).get(j).get("Close") - (Double) stockValues.get(stockValues.size() - 2).get(j).get("Open");
                            recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                        } else {
                            double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                            tmp3[0] = 0;
                            tmp3[1] = 0;
                            recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                        }

                    } else if ((Double) stockValues.get(stockValues.size() - 1).get(j).get("Close") < (Double) stockValues.get(stockValues.size() - 2).get(j).get("Open")) {
                        {
                            if (recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"))[0] <= 0.0) {
                                double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                                tmp3[0]--;
                                tmp3[1] += (Double) stockValues.get(stockValues.size() - 1).get(j).get("Close") - (Double) stockValues.get(stockValues.size() - 2).get(j).get("Open");
                                recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                            } else {
                                double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                                tmp3[0] = 0;
                                tmp3[1] = 0;
                                recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                            }
                        }

                    } else {
                        if ((Double) stockValues.get(stockValues.size() - 1).get(j).get("Open") > (Double) stockValues.get(stockValues.size() - 2).get(j).get("Close")) {

                            if (recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"))[0] >= 0.0) {

                                double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                                tmp3[0]++;
                                tmp3[1] += (Double) stockValues.get(stockValues.size() - 1).get(j).get("Open") - (Double) stockValues.get(stockValues.size() - 2).get(j).get("Close");
                                recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                            } else {
                                double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                                tmp3[0] = 0;
                                tmp3[1] = 0;
                                recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                            }

                        } else if ((Double) stockValues.get(stockValues.size() - 1).get(j).get("Open") < (Double) stockValues.get(stockValues.size() - 2).get(j).get("Close")) {
                            {
                                if (recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"))[0] <= 0.0) {
                                    double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                                    tmp3[0]--;
                                    tmp3[1] += (Double) stockValues.get(stockValues.size() - 1).get(j).get("Open") - (Double) stockValues.get(stockValues.size() - 2).get(j).get("Close");
                                    recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                                } else {
                                    double[] tmp3 = recordVariationMap.get(stockValues.get(stockValues.size() - 1).get(j).get("Symbol"));
                                    tmp3[0] = 0;
                                    tmp3[1] = 0;
                                    recordVariationMap.put((String) stockValues.get(stockValues.size() - 1).get(j).get("Symbol"), tmp3);
                                }
                            }
                        }
                    }

                }

            }
        }
        //System.out.println(recordVariationMap.get("BABA")[0] +" ," +
        //        " "+recordVariationMap.get("INTC")[0]);

    }


public double searchStockPrice(String symbol){
    for(int i=0;i<stockValues.get(stockValues.size()-1).size();i++){
       if( symbol == stockValues.get(stockValues.size()-1).get(i).get("Symbol")){
           if(stockValues.get(stockValues.size()-1).get(i).size() == 5){
               return (Double) stockValues.get(stockValues.size()-1).get(i).get("Close");
           }else{
               return (Double) stockValues.get(stockValues.size()-1).get(i).get("Open");
           }
       }
    }
    return -1;
}

public void decisionFunc(){

    for (Map.Entry<String, double[]> stock : recordVariationMap.entrySet()) {
        if(stock.getValue()[0] >= 1){
            //vender stocks aqui

            sellStock(stock.getKey(),searchStockPrice(stock.getKey()),5);

        }else if(stock.getValue()[0] <= - 1){
            //comprar aqui
            buyStock(stock.getKey(),searchStockPrice(stock.getKey()),5);
        }
    }
}

    private void buyStock(final String name, final double price, final int numsShares){

        SServiceProvider.getService(agent, AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<AgentRequestService>() {
                    public void resultAvailable(AgentRequestService service) {
                        service.BuyStocksRequest(agent.getComponentIdentifier(), name, numsShares, price);
                    }
                });
    }

    private void sellStock(final String name, final double price, final int numsShares){

        SServiceProvider.getService(agent, AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<AgentRequestService>() {
                    public void resultAvailable(AgentRequestService service) {
                        service.SellStockRequest(agent.getComponentIdentifier(), name, numsShares, price);
                    }
                });
    }

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price) {

        if(agentid == this.agent.getComponentIdentifier()) { //confirmaçao do mercado
            if(money >= quantity*price){

                money -= quantity*price;

                stockHist.put(stockname,quantity);

                //guardar a stock
                if(stocksOwned.get(stockname)!= null){
                    stocksOwned.put(stockname,stocksOwned.get(stockname) + quantity);
                }else{
                    stocksOwned.put(stockname,quantity);
                }

                SServiceProvider.getService(agent, AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                        .addResultListener(new DefaultResultListener<AgentChatService>() {
                            public void resultAvailable(AgentChatService service) {
                                service.BuyStockMessage(agentid, stockname, quantity, price);
                            }
                        });

                System.out.println("type1 agent comprou stock: "+ stockname + ": " + quantity);
                System.out.println(stocksOwned);

            }else{
                //nao tem guito para comprar
                System.out.println("rip");
            }

        }

        return null;
    }

    public IFuture<Void> ConfirmStockSell(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price) {
        if(stocksOwned.get(stockname)-quantity <= 0) {
            stocksOwned.remove(stockname);
        }else{

            stocksOwned.put(stockname, stocksOwned.get(stockname) - quantity);

        }

        money += quantity*price;


        SServiceProvider.getService(agent, AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<AgentChatService>() {
                    public void resultAvailable(AgentChatService service) {
                        service.SellStockMessage(agentid, stockname, quantity, price);
                    }
                });

        System.out.println("Agent type1 vendeu saldo: "+ money);
        System.out.println(stocksOwned);

        return null;
    }


    public IFuture<Void> BuyStockMessage(IComponentIdentifier agentid, String stockname, int quantity, double price /* 0 for send, 1 for receive */) {
        if(agentid == this.agent.getComponentIdentifier()){
            System.out.println("Sou eu, vou ignorar");
            return null;
        }
        else{
            System.out.println("O agente com o id " + agentid + " comprou " + quantity + " stocks de " + stockname);
        }
        return null;
    }

    public IFuture<Void> SellStockMessage(IComponentIdentifier agentid, String stockname, int quantity, double price) {
        if(agentid == this.agent.getComponentIdentifier()){
            System.out.println("Sou eu, vou ignorar");
            return null;
        }
        else {
            System.out.println("O agente com o id " + agentid + " vendeu " + quantity + " stocks de " + stockname);
        }
        return null;
    }
}
