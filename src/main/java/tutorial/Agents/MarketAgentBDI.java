package tutorial.Agents;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import jadex.rules.eca.ChangeInfo;
import tutorial.GUI.markerGUI;
import tutorial.Services.AgentRequestService;
import tutorial.Services.MarketAgentService;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Agent
@ProvidedServices(@ProvidedService(type=AgentRequestService.class))
@Description("Market.")
public class MarketAgentBDI implements AgentRequestService {

    private Map<String, Stock> stocks;
    private Map<String,List<HistoricalQuote>> stockHist;
    private int days;
    private ArrayList<HashMap> sendMarketVal;
    private String[] symbols = new String[] {"INTC", "BABA", "TSLA", "YHOO", "GOOG"};

    @Belief
    private boolean openstatus;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate = 3000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    IInternalAccess agent;

    public markerGUI GUI;

    @AgentCreated
    private void init(){
        GUI = new markerGUI();
        try {
            getStocksHist();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initMarket();
        days = stockHist.get("INTC").size()-1;
        openstatus = true;
    }

    @AgentBody
    private void body(){

        bdiFeature.adoptPlan("updateMarketPlan");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame();
                f.setSize(new Dimension(300,130));
                f.setContentPane(GUI.panelMain);
                f.setTitle("Market Agent");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }

    public void updateMarket(){
        if(days < 0)
            return; //chegou ao fim dos dias

        for(int i = 0; i < symbols.length; i++){
            Market.get(symbols[i]).add(stockHist.get(symbols[i]).get(days));
        }
        sendMarketVal = new ArrayList<HashMap>();
        createLastQuoteHash(sendMarketVal);

        if(openstatus){

            DefaultListModel listModel = new DefaultListModel();
            for(int i=0;i<symbols.length;i++){
                listModel.addElement(Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getSymbol() + " : " + Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getOpen().doubleValue());
            }

            GUI.stockValuesGUI.setModel(listModel);

            openstatus = !openstatus;
        }
        else {
            DefaultListModel listModel = new DefaultListModel();
            for(int i=0;i<symbols.length;i++){
                listModel.addElement(Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getSymbol() + " : " + Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getClose().doubleValue());
            }

            GUI.stockValuesGUI.setModel(listModel);

            openstatus = !openstatus;

            days--;
        }
    }

    @Plan(trigger=@Trigger(factchangeds = "time"))
    protected void updateMarketPlan(ChangeEvent event){
        ChangeInfo<Long> change = (ChangeInfo<Long>)event.getValue();
        if(change.getOldValue() == change.getValue())
            return;
        updateMarket();
    }

    private void initMarket(){
        Market = new HashMap<String,List<HistoricalQuote>>();

        for(int i = 0; i < symbols.length; i++) {
            Market.put(symbols[i], new ArrayList());
        }
    }

    private void getStocksHist() throws IOException {
        stockHist = new HashMap<String,List<HistoricalQuote>>();

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1); // from 1 year ago
        stocks = YahooFinance.get(symbols, true);

        for(int i = 0; i < symbols.length; i++){
            stockHist.put(symbols[i], stocks.get(symbols[i]).getHistory(from, to, Interval.DAILY));
        }
    }

    @Plan(trigger=@Trigger(factchangeds = "openstatus"))
    public IFuture<Void> UpdateMarketService() {
        SServiceProvider.getServices(agent.getServiceProvider(), MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<MarketAgentService>() {
            public void intermediateResultAvailable(MarketAgentService is) {
                is.UpdateMarketService(sendMarketVal);
            }
        });

        return null;
    }

    public void createLastQuoteHash(ArrayList<HashMap> quote){

        for(int i = 0; i < symbols.length; i++){
            HashMap temp = new HashMap();
            temp.put("Symbol", symbols[i]);

            if(openstatus)
                temp.put("Open", Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getOpen().doubleValue());
            else{
                temp.put("Close", Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getClose().doubleValue());
                temp.put("High", Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getHigh().doubleValue());
                temp.put("Low", Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getLow().doubleValue());
                temp.put("Volume", Market.get(symbols[i]).get(Market.get(symbols[i]).size()-1).getVolume().intValue());

            }
            quote.add(temp);
        }
    }

    public void sendLastMarketValues(MarketAgentService service, ArrayList<HashMap> message) {
        service.UpdateMarketService(message);
    }

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price,final int type, final double agent_winrate){
        SServiceProvider.getServices(agent.getServiceProvider(), MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<MarketAgentService>() {
            public void intermediateResultAvailable(MarketAgentService is) {
                is.ConfirmStockBuy(agentid, stockname, quantity, price, type, agent_winrate);
            }
        });
        return null;
    }

    public IFuture<Void> BuyStocksRequest(IComponentIdentifier agentid, String stockname, int quantity, double price,final int type, final double agent_winrate) {

        if(!openstatus) {
            if (Market.get(stockname).get(Market.get(stockname).size()-1).getOpen().doubleValue() == price) {
                ConfirmStockBuy(agentid, stockname, quantity, price,type, agent_winrate);
            }
        }
        else{
            if (Market.get(stockname).get(Market.get(stockname).size()-2).getClose().doubleValue() == price) {
                ConfirmStockBuy(agentid, stockname, quantity, price,type, agent_winrate);
            }
        }
        return null;
    }

    public IFuture<Void> SellStockRequest(IComponentIdentifier agentid, String stockname, int quantity, double price,final int type, final double agent_winrate) {
        if(!openstatus) {
            if (Market.get(stockname).get(Market.get(stockname).size()-1).getOpen().doubleValue() == price) {
                ConfirmStockSell(agentid, stockname, quantity, price,type, agent_winrate);
            }
            else {
                System.out.println("Demoraste muito a vender!");
            }
        }
        else{
            if (Market.get(stockname).get(Market.get(stockname).size()-2).getClose().doubleValue() == price)
                ConfirmStockSell(agentid, stockname, quantity, price, type, agent_winrate);
            else
                System.out.println("Demoraste muito a vender!2");
        }

        return null;
    }

    public IFuture<Void> ConfirmStockSell(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price, final int type, final double agent_winrate){
        SServiceProvider.getServices(agent.getServiceProvider(), MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new IntermediateDefaultResultListener<MarketAgentService>() {
            public void intermediateResultAvailable(MarketAgentService is) {
                is.ConfirmStockSell(agentid, stockname, quantity, price,type, agent_winrate);
            }
        });
        return null;
    }
}
