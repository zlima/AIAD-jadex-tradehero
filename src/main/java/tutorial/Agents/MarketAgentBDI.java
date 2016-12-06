package tutorial.Agents;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.gui.PropertiesPanel;
import jadex.commons.gui.SGUI;
import jadex.micro.annotation.*;
import jadex.rules.eca.ChangeInfo;
import org.apache.batik.bridge.Mark;
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

import tutorial.GUI.markerGUI;

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
    private ArrayList<HashMap> sendMarketVal;

    @Belief
    private boolean openstatus;

    @Belief
    private int dayspassed;

    @Belief
    private String[] symbols = new String[] {"INTC", "BABA", "TSLA", "YHOO", "GOOG"};

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate = 1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    IInternalAccess agent;

    public markerGUI GUI;

    @AgentCreated
    private void init(){
        GUI = new markerGUI();
        dayspassed = 0;
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
                f.setContentPane(GUI.panelMain);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.pack();
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
                dayspassed++;


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
        SServiceProvider.getService(agent, MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<MarketAgentService>() {
                    public void resultAvailable(MarketAgentService service) {
                        System.out.println(sendMarketVal);
                        sendLastMarketValues(service,sendMarketVal);
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

    public IFuture<Void> ConfirmStockBuy(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price){
        SServiceProvider.getService(agent, MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<MarketAgentService>() {
                    public void resultAvailable(MarketAgentService service) {
                        service.ConfirmStockBuy(agentid, stockname, quantity, price);
                    }
                });
        return null;
    }

    public IFuture<Void> BuyStocksRequest(IComponentIdentifier agentid, String stockname, int quantity, double price) {

        if(!openstatus) {
            if (Market.get(stockname).get(Market.get(stockname).size()-1).getOpen().doubleValue() == price) {
                ConfirmStockBuy(agentid, stockname, quantity, price);
            }
        }
        else{
            if (Market.get(stockname).get(Market.get(stockname).size()-2).getClose().doubleValue() == price) {
                ConfirmStockBuy(agentid, stockname, quantity, price);
            }
        }
        return null;
    }

    public IFuture<Void> SellStockRequest(IComponentIdentifier agentid, String stockname, int quantity, double price) {
        if(!openstatus) {
            if (Market.get(stockname).get(Market.get(stockname).size()-1).getOpen().doubleValue() == price) {
                /*System.out.print("Valor 1: ");
                System.out.println(Market.get(stockname).get(dayspassed).getOpen().doubleValue());
                System.out.print("Valor 2: ");
                System.out.println(price);*/
                ConfirmStockSell(agentid, stockname, quantity, price);
            }
            else {
               /* System.out.print("Valor 1: ");
                System.out.println(Market.get(stockname).get(dayspassed).getOpen().doubleValue());
                System.out.print("Valor 1 close: ");
                System.out.println(Market.get(stockname).get(dayspassed).getClose().doubleValue());
                System.out.print("Valor 2: ");
                System.out.println(price);*/
                System.out.println("Demoraste muito a vender!");
            }
        }
        else{
            if (Market.get(stockname).get(Market.get(stockname).size()-2).getClose().doubleValue() == price)
                ConfirmStockSell(agentid, stockname, quantity, price);
            else
                System.out.println("Demoraste muito a vender!2");
        }

        return null;
    }

    public IFuture<Void> ConfirmStockSell(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price){
        SServiceProvider.getService(agent, MarketAgentService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<MarketAgentService>() {
                    public void resultAvailable(MarketAgentService service) {
                        //System.out.println("Envio de mensagem de venda");
                        service.ConfirmStockSell(agentid, stockname, quantity, price);
                    }
                });
        return null;
    }
}
