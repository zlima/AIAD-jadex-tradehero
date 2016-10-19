package tutorial;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.component.IExecutionFeature;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;

import java.text.SimpleDateFormat;

/**
 * Created by Cenas on 10/8/2016.
 */

@Agent
public class ClockBDI {


    protected SimpleDateFormat formatter;

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @AgentCreated
    private void init(){

        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    }

    @AgentFeature
    protected IExecutionFeature execFeature;

    @AgentBody
    private void body(){

            bdiFeature.adoptPlan("printTime");
    }

    @Belief
    public long getTime()
    {
        return time;
    }

    @Belief(updaterate=1000)
    protected long time = System.currentTimeMillis();

    @Plan(trigger=@Trigger(factchangeds="time"))
    protected void printTime()
    {
        System.out.println(formatter.format(getTime()));
    }

}
