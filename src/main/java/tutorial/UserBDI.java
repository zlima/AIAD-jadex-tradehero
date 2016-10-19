package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.commons.gui.PropertiesPanel;
import jadex.commons.gui.SGUI;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by Cenas on 10/12/2016.
 */
@Agent
public class UserBDI
{
    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    protected IInternalAccess agent;

    @AgentBody
    public void body()
    {
        try {
            StockTest cenas = new StockTest();
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() {
                JFrame f = new JFrame();
                PropertiesPanel pp = new PropertiesPanel();
                final JTextField tfe = pp.createTextField("English Word", "dog", true);
                final JTextField tfg = pp.createTextField("German Word");
                JButton bt = pp.createButton("Initiate", "Translate");
                f.add(pp, BorderLayout.CENTER);
                f.pack();
                f.setLocation(SGUI.calculateMiddlePosition(f));
                f.setVisible(true);

                bt.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SServiceProvider.getServices(agent, ITranslationService.class, RequiredServiceInfo.SCOPE_PLATFORM)
                                .addResultListener(new IntermediateDefaultResultListener<ITranslationService>()
                                {
                                    public void intermediateResultAvailable(ITranslationService ts)
                                    {
                                        tfg.setText(ts.translateEnglishGerman(tfe.getText()).get());

                                    }
                                });
                    }
                });

            }
        });*/
    }
}