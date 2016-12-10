package tutorial;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.ThreadSuspendable;

public class Menu {
    IComponentManagementService cms;
    ThreadSuspendable sus;

    public Menu(IComponentManagementService cms, ThreadSuspendable sus) {
        this.cms=cms;
        this.sus=sus;

        initialize();
    }

    private void initialize() {
        IComponentIdentifier wid = cms.createComponent("tutorial/Agents/MarketAgentBDI.class", null).getFirstResult(sus);

        IComponentIdentifier cID = cms.createComponent( "randomagent", "tutorial/Agents/RandomAgentBDI.class", null).getFirstResult(sus);

        IComponentIdentifier cID2 = cms.createComponent( "type1agent","tutorial/Agents/Type1AgentBDI.class", null).getFirstResult(sus);
    }
}
