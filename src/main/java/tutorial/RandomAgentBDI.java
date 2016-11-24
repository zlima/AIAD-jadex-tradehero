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

import java.util.*;

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
    private Map<String,Integer>stockHist;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Belief
    private boolean updatedstock;

    protected ArrayList<ArrayList<HashMap>> stockValues;

    @Agent
    IInternalAccess agent;

    @AgentCreated
    private void init(){
        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        winrate = 0.0;
        money = 400000;
        stockValues = new ArrayList<ArrayList<HashMap>>();
        stockHist = new HashMap<String, Integer>();
    }

    @AgentBody
    private void body(){

    }





    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {

        stockValues.add(quote);
        updatedstock = !updatedstock;

       // System.out.println(stockValues.get(stockValues.size()-1).get(0).get("Symbol"));
        //parsetoStock(quote);
        //System.out.println(quote.get(0).getOpen());
        return null;
    }

    public IFuture<Void> ConfirmStockBuy(IComponentIdentifier agentid, String stockname, int quantity, double price) {
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

                System.out.println("random agent comprou stock: "+ stockname + ": " + quantity);

            }else{
                //nao tem guito para comprar
                System.out.println("rip");
            }

        }
        return null;
    }


    public IFuture<Void> ConfirmStockSell(IComponentIdentifier agentid, String stockname, int quantity, double price) {
        System.out.println("ndef");
        if(stocksOwned.get(stockname)-quantity <= 0) {
            stocksOwned.remove(stockname);
        }else{

            stocksOwned.put(stockname, stocksOwned.get(stockname) - quantity);

        }

            money += quantity*price;

        System.out.println("vendeu   saldo: "+money);


        return null;
    }



    @Plan(trigger=@Trigger(factchangeds="updatedstock"))
    public void newStockvalues(){
        Random rand = new Random();
        int  n = rand.nextInt(3);

        switch (n){
            case 0:
                buyStock();
                break;
            case 1:
                sellStock();
                break;
            case 2:
                System.out.println("ignora");
                break;//nao fazer nada
        }
    }




    private void sellStock(){

        if(stocksOwned.size() == 0){
            System.out.println("ignora, sem stocks");
            return;
        }
        SServiceProvider.getService(agent, AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<AgentRequestService>() {
                    public void resultAvailable(AgentRequestService service) {
                        Random rand = new Random();
                        int n = rand.nextInt(stocksOwned.size());
                        Object[] value2 = stocksOwned.keySet().toArray();
                        String symbol = (String) value2[n];
                        int rand2 = rand.nextInt(stocksOwned.get(symbol)+1);

                        int j = 0;
                        for(int i = 0; i < stockValues.size(); i++){
                            ArrayList<HashMap> temp = stockValues.get(i);
                            for(j = 0; j < temp.size(); j++){
                                if(symbol == temp.get(j).get("Symbol"))
                                    break;
                            }
                        }

                        if (stockValues.get(stockValues.size() - 1).get(0).size() > 2) {
                            service.SellStockRequest(agent.getComponentIdentifier(), symbol
                                    , rand2,(Double) stockValues.get(stockValues.size() - 1).get(j).get("Close") );
                        }else{
                            service.SellStockRequest(agent.getComponentIdentifier(), symbol
                                    , rand2,(Double) stockValues.get(stockValues.size() - 1).get(j).get("Open") );
                        }
                    }
                });
                }


    private void buyStock(){
            SServiceProvider.getService(agent, AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                    .addResultListener(new DefaultResultListener<AgentRequestService>() {
                        public void resultAvailable(AgentRequestService service) {
                            Random rand = new Random();
                            int n = rand.nextInt(stockValues.get(stockValues.size() - 1).size());//escolher uma stock para comprar

                            if (stockValues.get(stockValues.size() - 1).get(n).size() > 2) {//close

                                double test = money / (Double) stockValues.get(stockValues.size() - 1).get(n).get("Close");
                                int rand2 = rand.nextInt((int) test);
                                service.BuyStocksRequest(agent.getComponentIdentifier(), (String) stockValues.get(stockValues.size() - 1).get(n).get("Symbol"), rand2,
                                        (Double) stockValues.get(stockValues.size() - 1).get(n).get("Close"));
                            } else {
                                double test = money / (Double) stockValues.get(stockValues.size() - 1).get(n).get("Open");
                                int rand2 = rand.nextInt((int) test);
                                service.BuyStocksRequest(agent.getComponentIdentifier(), (String) stockValues.get(stockValues.size() - 1).get(n).get("Symbol"), rand2,
                                        (Double) stockValues.get(stockValues.size() - 1).get(n).get("Open"));
                            }
                        }
                        });
        }


}
