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
import javax.xml.bind.SchemaOutputResolver;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Agent
@Arguments({
        @Argument(name="money", clazz=Double.class, defaultvalue="9999.99"),
        @Argument(name="numfollow", clazz=Integer.class, defaultvalue="3"),
        @Argument(name="numComprar", clazz=Integer.class, defaultvalue="3"),
        @Argument(name="numVender", clazz=Integer.class, defaultvalue="3")
})
@Service
@ProvidedServices({
        @ProvidedService(type=MarketAgentService.class),
        @ProvidedService(type=AgentChatService.class)
})
@Description("Type1 Agent")
public class Type1AgentBDI implements MarketAgentService, AgentChatService  {

    private double money; //dinheiro do agent
    private double minrate_follow;
    private double winrate;
    private Map<String,Integer> stocksOwned;
    private Map<String,Integer> stockHist;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief
    private List<IComponentIdentifier> followers;

    @Belief
    private List<IComponentIdentifier> following;

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

    private TraderGUI GUI;

    protected HashMap<String,Double> followersGains;
    private int numfollow,numComprar,numVender;

    @AgentCreated
    private void init(){
        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        stockValues = new ArrayList<ArrayList<HashMap>>();
        stockHist = new HashMap<String, Integer>();
        recordVariationMap = new HashMap<String, double[]>();
        GUI = new TraderGUI();
        followers = new ArrayList<IComponentIdentifier>();
        following = new ArrayList<IComponentIdentifier>();
        followersGains = new HashMap<String, Double>();
        this.winrate = 0;
        this.minrate_follow = (Double) agent.getArgument("minRate");
        this.money = (Double) agent.getArgument("money");
        this.numfollow = (Integer) agent.getArgument("numfollow");
        this.numComprar = (Integer) agent.getArgument("numComprar");
        this.numVender = (Integer) agent.getArgument("numVender");
    }

    @AgentBody
    private void body(){
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

    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote) {
        stockValues.add(quote);

        updatedstock = !updatedstock;

        return null;
    }

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
        if(stock.getValue()[0] >= numVender){
            //vender stocks aqui
            sellStock(stock.getKey(),searchStockPrice(stock.getKey()),5,1, winrate);
        }else if(stock.getValue()[0] <= - numComprar){
            //comprar aqui
            buyStock(stock.getKey(),searchStockPrice(stock.getKey()),5,1, winrate);
        }
    }
}

    private void buyStock(final String name, final double price, final int numsShares, final int type, final double agent_winrate){
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

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final int type,final double agent_winrate) {
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
                System.out.println("Vou comprar");
                if(type==1) {
                    SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                        public void intermediateResultAvailable(AgentChatService service) {
                            service.BuyStockMessage(agentid,stockname,quantity,price, agent_winrate);
                        }
                    });
                    updateGUI();
                }
            }else{
                //System.out.println("nao é para mim");
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
        System.out.println("Vou vender");
        updateGUI();
        if(type==1) {
            SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                public void intermediateResultAvailable(AgentChatService service) {
                    service.SellStockMessage(agentid,stockname,quantity,price, agent_winrate);
                }
            });
        }
        return null;
    }

    public IFuture<Void> BuyStockMessage(final IComponentIdentifier agentid, String stockname, int quantity, double price, double agent_winrate /* 0 for send, 1 for receive */) {
        if(agentid == this.agent.getComponentIdentifier()){
           //System.out.println("Sou eu, vou ignorar");
            return null;
        }else{
            if (following.size() < numfollow){
                if(!following.contains(agentid)){
                    System.out.println("sigo?" + agent_winrate + "<>" + minrate_follow);
                    if(agent_winrate > minrate_follow) {
                        System.out.println("vou seguir");
                        following.add(agentid);
                        DefaultListModel listModel = new DefaultListModel();
                        for (int i = 0; i < following.size(); i++) {
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
                }else{
                    System.out.println("deixo de seguir?" +agent_winrate + "<>" + minrate_follow);
                    if(agent_winrate < minrate_follow) {
                        System.out.println("vou deixar de seguir");
                        following.remove(agentid);
                        DefaultListModel listModel = new DefaultListModel();
                        for (int i = 0; i < following.size(); i++) {
                            listModel.addElement(following.get(i));
                        }

                        GUI.seguirList.setModel(listModel);
                        //Enviar mensagem a dizer que deixou de seguir
                        SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                            public void intermediateResultAvailable(AgentChatService service) {
                                service.UnfollowMessage(agentid, agent.getComponentIdentifier());
                            }
                        });
                    }
                }
            }else{
                //Nao pode seguir mais ninguém
                if(following.contains(agentid)) {
                    System.out.println("deixo de seguir?" +agent_winrate + "<>" + minrate_follow);
                    if(agent_winrate < minrate_follow) {
                        System.out.println("vou deixar de seguir");
                        following.remove(agentid);
                        DefaultListModel listModel = new DefaultListModel();
                        for (int i = 0; i < following.size(); i++) {
                            listModel.addElement(following.get(i));
                        }

                        GUI.seguirList.setModel(listModel);
                        //Enviar mensagem a dizer que deixou de seguir
                        SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                            public void intermediateResultAvailable(AgentChatService service) {
                                service.UnfollowMessage(agentid, agent.getComponentIdentifier());
                            }
                        });

                    }else {
                        System.out.println("O agente com o id " + agentid + " comprou " + quantity + " stocks de " + stockname);
                        buyStock(stockname, price, quantity, 0, agent_winrate);
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
            //System.out.println("Sou eu, vou ignorar");
            return null;
        }
        else {
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
                System.out.println("deixo de seguir?" +agent_winrate + "<>" + minrate_follow);
                if(agent_winrate < minrate_follow) {
                    System.out.println("vou deixar de seguir");
                    following.remove(agentid);
                    DefaultListModel listModel = new DefaultListModel();
                    for (int i = 0; i < following.size(); i++) {
                        listModel.addElement(following.get(i));
                    }

                    GUI.seguirList.setModel(listModel);
                    //Enviar mensagem a dizer que deixou de seguir
                    SServiceProvider.getServices(agent.getServiceProvider(), AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<AgentChatService>() {
                        public void intermediateResultAvailable(AgentChatService service) {
                            service.UnfollowMessage(agentid, agent.getComponentIdentifier());
                        }
                    });
                }
            }else{
                System.out.println("devo seguir?" +agent_winrate + "<>" + minrate_follow);
                if(agent_winrate > minrate_follow && following.size() < numfollow) {
                    System.out.println("vou seguir");
                    following.add(agentid);
                    DefaultListModel listModel = new DefaultListModel();
                    for (int i = 0; i < following.size(); i++) {
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
        if(agentid == this.agent.getComponentIdentifier()){
            if(!followers.contains(followerid)){
                System.out.println("Adicionando seguidor");
                followers.add(followerid);
            }
            DefaultListModel listModel = new DefaultListModel();
            for (int i=0; i< followers.size();i++) {
                listModel.addElement(followers.get(i));
            }

            GUI.seguidoresList.setModel(listModel);
        }
        else{
            //O agentid está a seguir o followerid
        }
        return null;
    }

    public IFuture<Void> UnfollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid) {
        if(agentid == this.agent.getComponentIdentifier()){
            if(followers.contains(followerid)){
                System.out.println("Removendo seguidor");
                followers.remove(followerid);
            }
            DefaultListModel listModel = new DefaultListModel();
            for (int i=0; i< followers.size();i++) {
                listModel.addElement(followers.get(i));
            }

            GUI.seguidoresList.setModel(listModel);
        }
        return null;
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
        DefaultListModel listModel = new DefaultListModel();
        GUI.saldoGUI.setText(String.valueOf(money));
        for (Map.Entry<String, Integer> pair : stocksOwned.entrySet()) {
            listModel.addElement(pair.getKey() + " : " + pair.getValue());
        }

        GUI.stocksGUI.setModel(listModel);

        winrate = -(100 - (calcMoney() *100) / (Double) agent.getArgument("money"));
        GUI.winrateGUI.setText(String.valueOf(winrate) + "%");
        GUI.carteiraGUI.setText(String.valueOf(calcMoney()));
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
