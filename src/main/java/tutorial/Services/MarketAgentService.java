package tutorial.Services;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MarketAgentService {

    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote);
    public IFuture<Void> ConfirmStockBuy(IComponentIdentifier agentid, String stockname, int quantity, double price, int type);
    public IFuture<Void> ConfirmStockSell(IComponentIdentifier agentid, String stockname, int quantity, double price, int type);
}
