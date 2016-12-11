package tutorial.Agents;

import jadex.bdiv3.annotation.Belief;
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

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Agent
@Arguments({
        @Argument(name="money", clazz=Double.class, defaultvalue="N/A"),
        @Argument(name="numfollow", clazz=Integer.class, defaultvalue="3"),
})

@Service
@ProvidedServices({
        @ProvidedService(type=AgentChatService.class),
        @ProvidedService(type=MarketAgentService.class),
})
@Description("Passive Agent")
public class PassiveAgentBDI implements MarketAgentService, AgentChatService {

    @Agent
    protected IInternalAccess agent;

    @Belief
    private List<IComponentIdentifier> followers;

    @Belief
    private List<IComponentIdentifier> following;

    protected ArrayList<ArrayList<HashMap>> stockValues;

    private Map<String,Integer> stocksOwned;

    private double minrate_follow;

    private double winrate;

    private double money;

    private TraderGUI GUI;

    protected HashMap<String,Double> followersGains;

    private int numfollow;

    @AgentCreated
    private void init(){

        GUI = new TraderGUI();
        followers = new ArrayList<IComponentIdentifier>();
        following = new ArrayList<IComponentIdentifier>();
         stockValues = new ArrayList<ArrayList<HashMap>>();
        stocksOwned = new HashMap<String, Integer>();
        followersGains = new HashMap<String,Double>();
        this.winrate = 0;
        this.minrate_follow = (Double) agent.getArgument("minRate");
        this.money = (Double) agent.getArgument("money");
        this.numfollow = (Integer) agent.getArgument("numfollow");
    }

    @AgentBody
    private void body() throws InterruptedException {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setSize(new Dimension(300,130));
                f.setTitle(agent.getComponentIdentifier().getName());
                f.setContentPane(GUI.panel1);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                GUI.saldoGUI.setText(String.valueOf(money));
                GUI.winrateGUI.setText(String.valueOf(winrate) + "%");
                f.setVisible(true);
            }
        });
    }

    private void buyStock(final String name, final double price, final int numsShares, final int type, final double agent_winrate){

        //Envia pedido ao mercado para comprar stocks
        SServiceProvider.getServices(agent.getServiceProvider(), AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentRequestService>() {
            public void intermediateResultAvailable(AgentRequestService is) {
                is.BuyStocksRequest(agent.getComponentIdentifier(), name, numsShares, price, type, agent_winrate);
            }
        });

    }

    private void sellStock(final String name, final double price, final int numsShares, final int type, final double agent_winrate){

        SServiceProvider.getServices(agent.getServiceProvider(), AgentRequestService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentRequestService>() {
            public void intermediateResultAvailable(AgentRequestService is) {
                is.SellStockRequest(agent.getComponentIdentifier(), name, numsShares, price, type, agent_winrate);
            }
        });
    }

    public IFuture<Void> BuyStockMessage(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final double agent_winrate) {
        if(agentid == this.agent.getComponentIdentifier()){
            System.out.println("Sou eu, vou ignorar");
            return null;
        }
        else{
            if (following.size() < numfollow){
                if(!following.contains(agentid) && agent_winrate > winrate){
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
                }else if(following.contains(agentid) && agent_winrate < winrate){
                    following.remove(agentid);
                    DefaultListModel listModel = new DefaultListModel();
                    for (int i=0; i< following.size();i++) {
                        listModel.addElement(following.get(i));
                    }

                    GUI.seguirList.setModel(listModel);
                    //Enviar mensagem a dizer que está a seguir
                    SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                        public void intermediateResultAvailable(AgentChatService service) {
                            service.UnfollowMessage(agentid, agent.getComponentIdentifier());
                        }
                    });
                }
            }else{
                if(following.contains(agentid)) {
                    System.out.println("O agente com o id " + agentid + " comprou " + quantity + " stocks de " + stockname);
                    buyStock(stockname, price, quantity, 0, agent_winrate);

                    if(agent_winrate < winrate){
                        following.remove(agentid);
                        DefaultListModel listModel = new DefaultListModel();
                        for (int i=0; i< following.size();i++) {
                            listModel.addElement(following.get(i));
                        }

                        GUI.seguirList.setModel(listModel);
                        //Enviar mensagem a dizer que está a seguir
                        SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                            public void intermediateResultAvailable(AgentChatService service) {
                                service.UnfollowMessage(agentid, agent.getComponentIdentifier());
                            }
                        });
                    }
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
                this.money -= quantity*price*0.05;

                //enviar dinheiro
                SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                    public void intermediateResultAvailable(AgentChatService service) {
                        service.sendMoney(agentid, agent.getComponentIdentifier(),quantity*price*0.05);
                    }
                });

                if(agent_winrate < winrate){
                    following.remove(agentid);
                    DefaultListModel listModel = new DefaultListModel();
                    for (int i=0; i< following.size();i++) {
                        listModel.addElement(following.get(i));
                    }

                    GUI.seguirList.setModel(listModel);
                    //Enviar mensagem a dizer que está a seguir
                    SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                        public void intermediateResultAvailable(AgentChatService service) {
                            service.UnfollowMessage(agentid, agent.getComponentIdentifier());
                        }
                    });
                }
                //atualiza a GUI
                updateGUI();
            }else{
                if (following.size() < numfollow && agent_winrate > winrate){
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
            }
        }
        return null;
    }

    public IFuture<Void> FollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid) {
        return null;
    }

    public IFuture<Void> UnfollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid) {
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
    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {
        return null;
    }

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final int type, final double agent_winrate) {
        if(agentid == this.agent.getComponentIdentifier()) { //confirmaçao do mercado
            if(money >= quantity*price){

                money -= quantity*price;

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
                //nao tem dinheiro para para comprar
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
            System.out.println("Agent type1 vendeu saldo: "+ money);
            updateGUI();
        }


        return null;
    }

    public void updateGUI(){
        System.out.flush();
        GUI.saldoGUI.setText(String.valueOf(money));
        DefaultListModel listModel = new DefaultListModel();
        for (Map.Entry<String, Integer> pair : stocksOwned.entrySet()) {
            listModel.addElement(pair.getKey() + " : " + pair.getValue());
        }

        GUI.stocksGUI.setModel(listModel);

        winrate = -(100 - (calcMoney() *100) / (Double) agent.getArgument("money"));
        GUI.winrateGUI.setText(String.valueOf(winrate) + "%");
        GUI.carteiraGUI.setText(String.valueOf(calcMoney()));
    }

    public double calcMoney(){
        double moneyaux=0;
        for (Map.Entry<String, Integer> pair : stocksOwned.entrySet()) {

            moneyaux += pair.getValue() * searchStockPrice(pair.getKey());
        }

        return moneyaux+money;
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

}
