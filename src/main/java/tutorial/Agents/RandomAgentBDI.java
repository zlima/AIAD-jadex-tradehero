package tutorial.Agents;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import jadex.rules.eca.ChangeInfo;
import tutorial.GUI.TraderGUI;
import tutorial.Services.AgentRequestService;
import tutorial.Services.MarketAgentService;
import tutorial.Services.AgentChatService;
import yahoofinance.histquotes.HistoricalQuote;

import javax.swing.*;
import java.util.*;

/**
 * Created by Cenas on 10/19/2016.
 */

@Agent
@Service
@Arguments
@ProvidedServices({
        @ProvidedService(type=AgentChatService.class),
        @ProvidedService(type=MarketAgentService.class),
})
@Description("random agent")
public class RandomAgentBDI implements MarketAgentService, AgentChatService {

    private double money; //dinheiro do agent
    private double winrate;
    private Map<String,Integer> stocksOwned;
    private Map<String,Integer>stockHist;


    private Map<String,List<HistoricalQuote>> Market;

    @Agent
    protected IInternalAccess agent;
    @Belief
    private ArrayList<Integer> followers;
    @Belief

    private ArrayList<Integer> following;

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Belief
    private boolean updatedstock = false;


    protected ArrayList<ArrayList<HashMap>> stockValues;



    private TraderGUI GUI;

    @AgentCreated
    private void init(){

        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        winrate = 0.0;
        money = 400000;
        stockValues = new ArrayList<ArrayList<HashMap>>();
        stockHist = new HashMap<String, Integer>();
        GUI = new TraderGUI();
        //followers = new ArrayList<Integer>();
        //following = new ArrayList<Integer>();
    }

    @AgentBody
    private void body() throws InterruptedException {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setTitle(agent.getComponentIdentifier().getName());
                f.setContentPane(GUI.panel1);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.pack();
                GUI.saldoGUI.setText(String.valueOf(money));
                f.setVisible(true);
            }
        });
    }




    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {
        stockValues.add(quote);
        updatedstock = !updatedstock;

       // System.out.println(stockValues.get(stockValues.size()-1).get(0).get("Symbol"));
        //parsetoStock(quote);
        //System.out.println(quote.get(0).getOpen());
        return null;
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

                updateGUI();
                SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                    public void intermediateResultAvailable(AgentChatService service) {
                        service.BuyStockMessage(agentid, stockname, quantity, price);
                    }
                });

            }else{
                //nao tem guito para comprar
               // System.out.println("rip");
                System.out.println("nao é para mim");
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

        SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
            public void intermediateResultAvailable(AgentChatService service) {
                service.SellStockMessage(agentid, stockname, quantity, price);
            }
        });
      //  System.out.println("vendeu   saldo: "+money);
        updateGUI();

        return null;
    }

    @Plan(trigger=@Trigger(factchangeds="updatedstock"))
    public void newStockvalues(){

        Random rand = new Random();
        int  n = rand.nextInt(3);
       //System.out.println(agent.getComponentIdentifier().getName() +"yooooooooooooooooooooo" + " "+money);
        switch (n){
            case 0:
                buyStock();
                break;
            case 1:
                sellStock();
                break;
            case 2:
                //System.out.println("ignora");
                break;//nao fazer nada
        }
    }




    private void sellStock(){

        if(stocksOwned.size() == 0){
           // System.out.println("ignora, sem stocks");
            return;
        }

        SServiceProvider.getServices(agent.getServiceProvider(), AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentRequestService>() {
            public void intermediateResultAvailable(AgentRequestService is) {
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
                    is.SellStockRequest(agent.getComponentIdentifier(), symbol
                            , rand2,(Double) stockValues.get(stockValues.size() - 1).get(j).get("Close"));
                }else{
                    is.SellStockRequest(agent.getComponentIdentifier(), symbol
                            , rand2,(Double) stockValues.get(stockValues.size() - 1).get(j).get("Open") );
                }
            }
        });
        /*SServiceProvider.getService(agent, AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<AgentRequestService>() {
                    public void resultAvailable(AgentRequestService service) {
                        Random rand = new Random();
                        int n = rand.nextInt(stocksOwned.size());
                        Object[] value2 = stocksOwned.keySet().toArray();
                        symbol[0] = (String) value2[n];
                        rand2[0] = rand.nextInt(stocksOwned.get(symbol[0])+1);

                        j[0] = 0;
                        for(int i = 0; i < stockValues.size(); i++){
                            ArrayList<HashMap> temp = stockValues.get(i);
                            for(j[0] = 0; j[0] < temp.size(); j[0]++){
                                if(symbol[0] == temp.get(j[0]).get("Symbol"))
                                    break;
                            }
                        }

                        if (stockValues.get(stockValues.size() - 1).get(0).size() > 2) {
                            service.SellStockRequest(agent.getComponentIdentifier(), symbol[0]
                                    , rand2[0],(Double) stockValues.get(stockValues.size() - 1).get(j[0]).get("Close") );
                        }else{
                            service.SellStockRequest(agent.getComponentIdentifier(), symbol[0]
                                    , rand2[0],(Double) stockValues.get(stockValues.size() - 1).get(j[0]).get("Open") );
                        }
                    }
                });*/
                }


    private void buyStock(){

        SServiceProvider.getServices(agent.getServiceProvider(), AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentRequestService>() {
            public void intermediateResultAvailable(AgentRequestService is) {
                            Random rand = new Random();
                            int n = rand.nextInt(stockValues.get(stockValues.size() - 1).size());//escolher uma stock para comprar

                            if (stockValues.get(stockValues.size() - 1).get(n).size() > 2) {//close

                                double test = money / (Double) stockValues.get(stockValues.size() - 1).get(n).get("Close");
                                int rand2 = rand.nextInt((int) test);
                                is.BuyStocksRequest(agent.getComponentIdentifier(), (String) stockValues.get(stockValues.size() - 1).get(n).get("Symbol"), rand2,
                                        (Double) stockValues.get(stockValues.size() - 1).get(n).get("Close"));
                            } else {
                                double test = money / (Double) stockValues.get(stockValues.size() - 1).get(n).get("Open");
                                int rand2 = rand.nextInt((int) test);
                                is.BuyStocksRequest(agent.getComponentIdentifier(), (String) stockValues.get(stockValues.size() - 1).get(n).get("Symbol"), rand2,
                                        (Double) stockValues.get(stockValues.size() - 1).get(n).get("Open"));
                            }
                        }
                        });
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

    public double calcMoney(){
        double moneyaux=0;
        for (Map.Entry<String, Integer> pair : stocksOwned.entrySet()) {

            moneyaux += pair.getValue() * searchStockPrice(pair.getKey());
        }

        return moneyaux+money;
    }

    public void updateGUI(){
        GUI.saldoGUI.setText(String.valueOf(money));
        DefaultListModel listModel = new DefaultListModel();
        for (Map.Entry<String, Integer> pair : stocksOwned.entrySet()) {
            listModel.addElement(pair.getKey() + " : " + pair.getValue());
        }

        GUI.stocksGUI.setModel(listModel);

        GUI.carteiraGUI.setText(String.valueOf(calcMoney()));
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
