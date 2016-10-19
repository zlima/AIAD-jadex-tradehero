package tutorial;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.micro.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cenas on 10/9/2016.
 */
@Agent
public class MalucasBDI {

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

    @Belief
    protected Map<String, String> wordtable;

    @AgentCreated
    private void init(){
        wordtable = new HashMap<String, String>();
        wordtable.put("oi","cenas");
        wordtable.put("coco","xixi");
    }

    @AgentBody
    private void body(){
        String eword = "coco";
        Translate goal = (Translate)bdiFeature.dispatchTopLevelGoal(new Translate(eword)).get();
        System.out.println("Translated: "+eword+" "+goal.getGWord());
    }

    @Plan(trigger=@Trigger(goals=Translate.class))
    protected void translate(Translate goal)
    {
        String eword = goal.getEWord();
        String gword = wordtable.get(eword);
        goal.setGWord(gword);
    }

}