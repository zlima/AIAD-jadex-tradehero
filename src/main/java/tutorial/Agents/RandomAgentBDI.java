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
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import tutorial.GUI.TraderGUI;
import tutorial.Services.AgentChatService;
import tutorial.Services.AgentRequestService;
import tutorial.Services.MarketAgentService;
import yahoofinance.histquotes.HistoricalQuote;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@Agent
@Service
@Arguments({
        @Argument(name="money", clazz=Double.class, defaultvalue="99.9"),
        @Argument(name="numfollow", clazz=Integer.class, defaultvalue="3"),
})

@ProvidedServices({
        @ProvidedService(type=AgentChatService.class),
        @ProvidedService(type=MarketAgentService.class),
})
@Description("Random Agent")
public class RandomAgentBDI implements MarketAgentService, AgentChatService {

    private double money; //dinheiro do agent
    private double winrate;
    private double minrate_follow;
    private Map<String,Integer> stocksOwned;
    private Map<String,Integer>stockHist;

    private Map<String,List<HistoricalQuote>> Market;

    @Agent
    protected IInternalAccess agent;

    @Belief
    private List<IComponentIdentifier> followers;

    @Belief
    private List<IComponentIdentifier> following;

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Belief
    private boolean updatedstock = false;

    protected ArrayList<ArrayList<HashMap>> stockValues;

    protected HashMap<String, Double> followersGains;

    private int numfollow;

    private TraderGUI GUI;

    @AgentCreated
    private void init(){

        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        winrate = 0.0;
        stockValues = new ArrayList<ArrayList<HashMap>>();
        stockHist = new HashMap<String, Integer>();
        GUI = new TraderGUI();
        followers = new ArrayList<IComponentIdentifier>();
        following = new ArrayList<IComponentIdentifier>();
        followersGains = new HashMap<String, Double>();
        this.winrate = 0;
        this.minrate_follow = 0;
        this.money = (Double) agent.getArgument("money");
        this.numfollow = (Integer) agent.getArgument("numfollow");
    }

    @AgentBody
    private void body() throws InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setSize(new Dimension(440,340));
                f.setTitle(agent.getComponentIdentifier().getName());
                f.setContentPane(GUI.panel1);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                GUI.saldoGUI.setText(String.valueOf(money));
                GUI.winrateGUI.setText(String.valueOf(winrate) + "%");
                f.setVisible(true);
            }
        });
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
                break;//nao fazer nada
        }
    }

    private void sellStock(){

        if(stocksOwned.size() == 0){
           //ignora, sem stocks
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
                            , rand2,(Double) stockValues.get(stockValues.size() - 1).get(j).get("Close"),1, winrate);
                }else{
                    is.SellStockRequest(agent.getComponentIdentifier(), symbol
                            , rand2,(Double) stockValues.get(stockValues.size() - 1).get(j).get("Open"),1, winrate );
                }
            }
        });
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
                            (Double) stockValues.get(stockValues.size() - 1).get(n).get("Close"),1, winrate);
                } else {
                    double test = money / (Double) stockValues.get(stockValues.size() - 1).get(n).get("Open");
                    int rand2 = rand.nextInt((int) test);
                    is.BuyStocksRequest(agent.getComponentIdentifier(), (String) stockValues.get(stockValues.size() - 1).get(n).get("Symbol"), rand2,
                            (Double) stockValues.get(stockValues.size() - 1).get(n).get("Open"),1, winrate);
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
        System.out.flush();
        GUI.saldoGUI.setText(String.valueOf(money));
        DefaultListModel listModel = new DefaultListModel();
        for (Map.Entry<String, Integer> pair : stocksOwned.entrySet()) {
            listModel.addElement(pair.getKey() + " : " + pair.getValue());
        }

        GUI.stocksGUI.setModel(listModel);

        winrate = ((calcMoney() *100) / (Double) agent.getArgument("money")-100);
        GUI.winrateGUI.setText(String.valueOf(winrate) + "%");
        GUI.carteiraGUI.setText(String.valueOf(calcMoney()));
    }

    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {
        stockValues.add(quote);
        updatedstock = !updatedstock;
        return null;
    }

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final int type, final double agent_winrate) {
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

                if(type==1) {
                    SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                        public void intermediateResultAvailable(AgentChatService service) {
                            service.BuyStockMessage(agentid,stockname,quantity,price, agent_winrate);
                        }
                    });
                }

            }else{
                System.out.println("nao é para mim");
            }
        }
        return null;
    }


    public IFuture<Void> ConfirmStockSell(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final int type, final double agent_winrate) {
        if(stocksOwned.get(stockname)-quantity <= 0) {
            stocksOwned.remove(stockname);
        }else{

            stocksOwned.put(stockname, stocksOwned.get(stockname) - quantity);

        }

        money += quantity*price;

        updateGUI();
        if(type==1) {
            SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                public void intermediateResultAvailable(AgentChatService service) {
                    service.SellStockMessage(agentid, stockname, quantity, price, agent_winrate);
                }
            });
        }
        return null;
    }

    public IFuture<Void> BuyStockMessage(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final double agent_winrate /* 0 for send, 1 for receive */) {
        if(agentid == this.agent.getComponentIdentifier()){
            return null;
        }
        else{
            if (following.size() < numfollow){
                if(!following.contains(agentid)){
                    following.add(agentid);
                    DefaultListModel listModel = new DefaultListModel();
                    for (int i=0; i< following.size();i++) {
                        listModel.addElement(following.get(i));
                    }

                    GUI.seguirList.setModel(listModel);
                    //Enviar mensagem a dizer que está a seguir
                    SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                        public void intermediateResultAvailable(AgentChatService service) {
                            service.FollowMessage(agentid, agent.getComponentIdentifier());
                        }
                    });
                }
                else{
                    System.out.println("Ja o estas a seguir");
                }
            }
            else{
                System.out.println("Nao podes seguir mais ninguém");
                if(following.contains(agentid)) {
                    System.out.println("O agente com o id " + agentid + " comprou " + quantity + " stocks de " + stockname);
                    buyStock(stockname, price, quantity, 0, agent_winrate);
                }
                return null;
            }
            if(following.contains(agentid)) {
                System.out.println("O agente com o id " + agentid + " comprou " + quantity + " stocks de " + stockname);
                buyStock(stockname, price, quantity, 0, agent_winrate);
            }
        }
        return null;
    }

    private void buyStock(final String name, final double price, final int numsShares, final int type, final double agent_winrate){
        SServiceProvider.getServices(agent.getServiceProvider(), AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentRequestService>() {
            public void intermediateResultAvailable(AgentRequestService is) {
                is.BuyStocksRequest(agent.getComponentIdentifier(), name, numsShares, price, type, agent_winrate);
            }
        });
    }

    public IFuture<Void> SellStockMessage(final IComponentIdentifier agentid, String stockname, final int quantity, final double price, final double agent_winrate) {
        if(agentid == this.agent.getComponentIdentifier()){
            System.out.println("Sou eu, vou ignorar");
            return null;
        }
        else {
            System.out.println("O agente com o id " + agentid + " vendeu " + quantity + " stocks de " + stockname);
            if(following.contains(agentid)){
                //vender e mandar dinhiro
                sellStock(stockname,price,quantity,0, agent_winrate);
                this.money -= quantity*price*0.005;
                //enviar dinheiro
                SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                    public void intermediateResultAvailable(AgentChatService service) {
                        service.sendMoney(agentid, agent.getComponentIdentifier(),quantity*price*0.005);
                    }
                });
            }
        }
        return null;
    }

    private void sellStock(final String name, final double price, final int numsShares, final int type, final double agent_winrate){

        SServiceProvider.getServices(agent.getServiceProvider(), AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentRequestService>() {
            public void intermediateResultAvailable(AgentRequestService is) {
                is.SellStockRequest(agent.getComponentIdentifier(), name, numsShares, price,type, agent_winrate);
            }
        });
    }

    public IFuture<Void> FollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid) {
        if(agentid == this.agent.getComponentIdentifier()){
            if(!followers.contains(followerid)){
                followers.add(followerid);
            }
            DefaultListModel listModel = new DefaultListModel();
            for (int i=0; i< followers.size();i++) {
                listModel.addElement(followers.get(i));
            }

            GUI.seguidoresList.setModel(listModel);
            System.out.println(agentid + " esta a ser seguido pelo " + followerid);
        }
        else{
            //O agentid está a seguir o followerid
        }
        return null;
    }

    public IFuture<Void> UnfollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid) {
        if(agentid == this.agent.getComponentIdentifier()){
            if(followers.contains(followerid)){
                followers.remove(followerid);
            }
            DefaultListModel listModel = new DefaultListModel();
            for (int i=0; i< followers.size();i++) {
                listModel.addElement(followers.get(i));
            }

            GUI.seguidoresList.setModel(listModel);
            System.out.println(agentid + " deixou de seguir o " + followerid);
        }
        else{
            //O agentid deixou de seguir o followerid
        }
        return null;
    }

    public IFuture<Void> sendMoney(IComponentIdentifier agentid, IComponentIdentifier senderid ,double qty) {
        if(this.agent.getComponentIdentifier() == agentid){
            if(!followersGains.containsKey(senderid.getName())){
                followersGains.put(senderid.getName(),qty);
                this.money += qty;
            }else{
                followersGains.put(senderid.getName(),followersGains.get(senderid.getName())+ qty);
                this.money += qty;
            }

            DefaultListModel listModel = new DefaultListModel();
            for (Map.Entry<String, Double> pair : followersGains.entrySet()) {
                listModel.addElement(pair.getKey() + " : " + pair.getValue());
            }
            System.out.flush();
            GUI.followerGainsGUI.setModel(listModel);
        }
        return null;
    }
}
