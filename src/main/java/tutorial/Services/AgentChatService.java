package tutorial.Services;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;

/**
 * Created by Fabio on 06/12/2016.
 */
public interface AgentChatService {
    public IFuture<Void> BuyStockMessage(IComponentIdentifier agentid, String stockname, int quantity, double price/*, int type*/);
    public IFuture<Void> SellStockMessage(IComponentIdentifier agentid, String stockname, int quantity, double price/*, int type*/);
}
