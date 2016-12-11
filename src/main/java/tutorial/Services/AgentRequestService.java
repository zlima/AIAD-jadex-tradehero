package tutorial.Services;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;

public interface AgentRequestService {
    public IFuture<Void> BuyStocksRequest(IComponentIdentifier agentid, String stockname, int quantity, double price, int type, double agent_winrate);
    public IFuture<Void> SellStockRequest(IComponentIdentifier agentid, String stockname, int quantity, double price, int type, double agent_winrate);
}
