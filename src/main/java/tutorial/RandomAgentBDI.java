package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.collection.SCollection;
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import yahoofinance.histquotes.HistoricalQuote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cenas on 10/19/2016.
 */

@Agent
@Service
@ProvidedServices(@ProvidedService(type=UpdateMarketService.class))
@Description("random agent")
public class RandomAgentBDI implements UpdateMarketService {

    private double money; //dinheiro do agent
    private double winrate;
    private Map<String,Integer> stocksOwned;

    @Belief
    private Map<String,List<HistoricalQuote>> Market;

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;




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



    public IFuture<Void> UpdateMarketService(final ArrayList<HashMap> quote) {

        System.out.println(quote.get(0).get("Close"));
        return null;
    }
}
