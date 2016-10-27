package tutorial.Services;

import jadex.commons.future.IFuture;

import java.util.HashMap;

/**
 * Created by Cenas on 10/27/2016.
 */
public interface AgentRequestService {

    public IFuture<Void> BuyStocksRequest();
}
