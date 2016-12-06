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
import tutorial.Services.AgentChatService;
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
@ProvidedServices(@ProvidedService(type=AgentChatService.class))
@Description("Chat.")
public class ChatAgentBDI implements AgentChatService{

    @Agent
    IInternalAccess agent;

    @AgentCreated
    private void init(){
    }

    @AgentBody
    private void body(){

    }

    public IFuture<Void> BuyStockMessage(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price) {
        System.out.println(agent.getComponentIdentifier());
        System.out.println(agentid);
        if(agentid == agent.getComponentIdentifier())
            System.out.println("Leu o proprio");
        else{
            System.out.println("O agente " + agentid + " comprou " + quantity + " stocks de " + stockname);
        }

        SServiceProvider.getService(agent, AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM)
            .addResultListener(new DefaultResultListener<AgentChatService>() {
                public void resultAvailable(AgentChatService service) {
                    service.BuyStockMessage(agentid, stockname, quantity, price);
                }
            });
        return null;
    }

    public IFuture<Void> SellStockMessage(final IComponentIdentifier agentid, final String stockname, final int quantity, final double price) {

        if(agentid == agent.getComponentIdentifier())
            System.out.println("Leu o proprio");
        else{
            System.out.println("O agente " + agentid + " vendeu " + quantity + " stocks de " + stockname);
        }
        SServiceProvider.getService(agent, AgentChatService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new DefaultResultListener<AgentChatService>() {
                    public void resultAvailable(AgentChatService service) {
                        service.SellStockMessage(agentid, stockname, quantity, price);
                    }
                });
        return null;
    }
}
