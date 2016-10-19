package tutorial;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.component.IExecutionFeature;
import jadex.micro.annotation.*;
import jadex.rules.eca.ChangeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cenas on 10/8/2016.
 */
@Agent
@Description("The translation agent B2. <br>  Declare and activate an inline plan (declared as inner class).")
public class TranslationBDI
{
    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @AgentFeature
    protected IExecutionFeature execFeature;

    @Belief
    protected Map<String, String> wordtable = new HashMap<String, String>();

    @Belief(dynamic=true)
    protected boolean alarm = wordtable.containsKey("bugger");

    @AgentCreated
    protected void init(){
        wordtable = new HashMap<String, String>();
        wordtable.put("bugger", "Flegel");
    }

    @AgentBody
    public void body()
    {
        try
        {
           // bdiFeature.adoptPlan(new TranslationPlan());
           // execFeature.waitForDelay(1000).get();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Plan(trigger=@Trigger(factchangeds="alarm"))
    public void checkWordPairPlan(ChangeEvent event)
    {
        ChangeInfo<Boolean> change = (ChangeInfo<Boolean>)event.getValue();
        // Print warning when value changes from false to true.
        if(Boolean.FALSE.equals(change.getOldValue()) && Boolean.TRUE.equals(change.getValue()))
        {
            System.out.println("Warning, a colloquial word pair has been added.");
        }
    }



}