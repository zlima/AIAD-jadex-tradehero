package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Goals;
import jadex.bdiv3.annotation.Plans;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.commons.gui.PropertiesPanel;
import jadex.commons.gui.SGUI;
import jadex.micro.annotation.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Cenas on 10/12/2016.
 */
@Agent

@RequiredServices(@RequiredService(name="transser", type=ITranslationService.class,
        binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)))
@Goals(@Goal(clazz=TranslationGoal.class))
public class User2BDI
{
    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Agent
    protected IInternalAccess agent;

    @AgentBody
    public void body()
    {
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
                      /*  execFeature.scheduleStep(new IComponentStep<Void>()
                        {
                            public IFuture<Void> execute(IInternalAccess ia)
                            {
                                try
                                {
                                    final String gword = (String)bdiFeature.dispatchTopLevelGoal(new TranslationGoal(tfe.getText())).get();
                                    // set word in textfield on swing thread
                                }
                                catch(Exception e)
                                {
                                    // set the exception message in textfield on swing thread
                                }
                            }
                        });*/
                    }
                });

            }
        });
    }
}