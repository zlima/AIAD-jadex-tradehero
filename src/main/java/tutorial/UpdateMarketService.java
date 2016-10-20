package tutorial;

import jadex.bridge.service.annotation.Service;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import yahoofinance.histquotes.HistoricalQuote;

import java.util.List;
import java.util.Map;

/**
 * Created by Cenas on 10/19/2016.
 */

public interface UpdateMarketService {

    public IFuture<Map<String,HistoricalQuote>> UpdateMarketService();
}
