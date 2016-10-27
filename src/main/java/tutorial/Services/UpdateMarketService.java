package tutorial.Services;

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

/**
 * Created by Cenas on 10/19/2016.
 */

public interface UpdateMarketService {

    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote);
}
