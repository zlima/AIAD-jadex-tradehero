package tutorial.Services;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;

public interface AgentChatService {
    public IFuture<Void> BuyStockMessage(IComponentIdentifier agentid, String stockname, int quantity, double price);
    public IFuture<Void> SellStockMessage(IComponentIdentifier agentid, String stockname, int quantity, double price);
    public IFuture<Void> FollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid);
    public IFuture<Void> UnfollowMessage(IComponentIdentifier agentid, IComponentIdentifier followerid);
    public IFuture<Void> sendMoney(IComponentIdentifier agentid,IComponentIdentifier senderid ,double qty);
}
