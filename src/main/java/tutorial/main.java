package tutorial;

/**
 * Created by Cenas on 12/6/2016.
 */


import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ThreadSuspendable;


import javax.swing.*;
import java.io.IOException;

/**
 * Created by jorgelima on 20-11-2014.
 */
public class main {


    public static void main(String[] args) throws IOException {

       // Jadex jadex = new Jadex();
        String[] defargs = new String[] {
                "-gui", "true",
                "-welcome", "false",
                "-cli", "false",
                "-printpass", "false",
                "-platformname", "AIAD"
        };


                PlatformConfiguration config  = PlatformConfiguration.processArgs(defargs);

                config.addComponent("tutorial.Agents.MarketAgentBDI.class");
                config.addComponent("tutorial.Agents.RandomAgentBDI.class");
                config.addComponent("tutorial.Agents.Type1AgentBDI.class");
                config.addComponent("tutorial.Agents.Type1AgentBDI.class");


                Starter.createPlatform(config).get();




       /* IFuture<IExternalAccess> platfut	= Starter.createPlatform(args);
        final ThreadSuspendable	sus	= new ThreadSuspendable();
        final IExternalAccess	platform	= platfut.get(sus);
        System.out.println("Started platform: "+ platform.getComponentIdentifier());


        IComponentManagementService cms = SServiceProvider.getService(platform.getServiceProvider(),
                IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM).get(sus);

        Menu menu = new Menu(cms, sus);*/


            }



}