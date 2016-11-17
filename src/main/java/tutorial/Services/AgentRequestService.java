package tutorial.Services;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;

import java.util.HashMap;

/**
 * Created by Cenas on 10/27/2016.
 */
public interface AgentRequestService {

    public IFuture<Void> BuyStocksRequest(IComponentIdentifier agentid, String stockname, int quantity, double price);
    public IFuture<Void> SellStockRequest(IComponentIdentifier agentid, String stockname, int quantity, double price);
}
