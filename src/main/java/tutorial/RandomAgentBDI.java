package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import yahoofinance.histquotes.HistoricalQuote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cenas on 10/19/2016.
 */

@Agent

@Description("random agent")
public class RandomAgentBDI {

    private double money; //dinheiro do agent
    private double winrate;
    private Map<String,Integer> stocksOwned;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    IInternalAccess agent;


    @AgentCreated
    private void init(){
        Market = new HashMap<String,List<HistoricalQuote>>();
        stocksOwned = new HashMap<String, Integer>();
        winrate = 0.0;
        money = 10000000;
    }

    @AgentBody
    private void body(){

    }


    public void getMarketValues(){

        SServiceProvider.getServices(agent, UpdateMarketService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                .addResultListener(new IntermediateDefaultResultListener<UpdateMarketService>() {
                    public void intermediateResultAvailable(UpdateMarketService up) {
                        System.out.println("oi");
                        System.out.println(up.UpdateMarketService());
                    }
                });
    }


    @Plan(trigger=@Trigger(factchangeds="time"))
    protected void updateMarketValues(){
        getMarketValues();
    }

}
