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
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ThreadSuspendable;
import tutorial.GUI.menuGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jorgelima on 20-11-2014.
 */
public class main {

    static IComponentManagementService cms;
    static ThreadSuspendable sus;
    static menuGUI GUI;
    static DefaultListModel listModel;

    public static void main(String[] args) throws IOException {
        init();
    }


        public static void init(){
            GUI = new menuGUI();
            initGUI();
            listModel = new DefaultListModel();

            // Jadex jadex = new Jadex();
            String[] defargs = new String[] {
                    "-gui", "false",
                    "-welcome", "false",
                    "-cli", "false",
                    "-printpass", "false",
                    "-platformname", "AIAD"
            };

            IFuture<IExternalAccess> platfut	= Starter.createPlatform(defargs);
            sus	= new ThreadSuspendable();
            final IExternalAccess	platform	= platfut.get(sus);
            cms = SServiceProvider.getService(platform.getServiceProvider(),
                    IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM).get(sus);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JFrame f = new JFrame();
                    f.setSize(new Dimension(960, 470));
                    f.setTitle("MENU");
                    f.setContentPane(GUI.panel);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
                }
            });
            cms.createComponent("tutorial.Agents.MarketAgentBDI.class",null).getFirstResult(sus);
        }

            public static void createNewPassive(Double money, int numfollow, Double minrate){

                Map<String, Object> agentArgs = new HashMap<String, Object>();
                agentArgs.put("money", money);
                agentArgs.put("numfollow", numfollow);
                agentArgs.put("minRate",minrate);
                CreationInfo agentInfo = new CreationInfo(agentArgs);
                listModel.addElement("Adicionado agente Passivo com " + money + "€ que pode seguir até " + numfollow + " agentes");
                GUI.agentesListGUI.setModel(listModel);
                cms.createComponent("tutorial.Agents.PassiveAgentBDI.class", agentInfo).getFirstResult(sus);

            }

    public static void createNewRand(Double money, int numfollow){

        Map<String, Object> agentArgs = new HashMap<String, Object>();
        agentArgs.put("money", money);
        agentArgs.put("numfollow", numfollow);
        CreationInfo agentInfo = new CreationInfo(agentArgs);
        listModel.addElement("Adicionado agente Random com " + money + "€ que pode seguir até " + numfollow + " agentes");
        GUI.agentesListGUI.setModel(listModel);
        cms.createComponent("tutorial.Agents.RandomAgentBDI.class", agentInfo).getFirstResult(sus);
    }


    public static void createNewTip1(Double money, int numfollow, int numComprar, int numVender, Double minrate){

        Map<String, Object> agentArgs = new HashMap<String, Object>();
        agentArgs.put("money", money);
        agentArgs.put("numfollow", numfollow);
        agentArgs.put("numComprar",numComprar);
        agentArgs.put("numVender",numVender);
        agentArgs.put("minRate",minrate);

        CreationInfo agentInfo = new CreationInfo(agentArgs);
        listModel.addElement("Adicionado agente do Tipo 1 com " + money + "€ que pode seguir até " + numfollow + " agentes");
        GUI.agentesListGUI.setModel(listModel);

        cms.createComponent("tutorial.Agents.Type1AgentBDI.class", agentInfo).getFirstResult(sus);
        System.out.println(cms.getComponentIdentifiers().get());
    }


    public static void initGUI() {

        GUI.seguirSpinnPassive.setValue(3);
        //ciar agente tipo1
        GUI.criarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewTip1((Double)GUI.saldoSpinnTip.getValue(),(Integer)GUI.seguirSpinnTip.getValue(),
                        (Integer)GUI.comprarSpinnTip.getValue(),(Integer)GUI.venderSpinnTip.getValue(), (Double)GUI.minrateSpinnTip.getValue());
            }
        });

        GUI.criarPassiveBT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
             createNewPassive((Double)GUI.saldoSpinnPassive.getValue(),(Integer)GUI.seguirSpinnPassive.getValue(), (Double)GUI.minrateSpinnTip.getValue());
            }
        });

        GUI.criarRandomBT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewRand((Double)GUI.saldoSpinnRand.getValue(),(Integer)GUI.seguirSpinnRand.getValue());
            }
        });
    }



}