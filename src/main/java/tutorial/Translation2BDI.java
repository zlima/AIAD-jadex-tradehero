package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Mapping;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cenas on 10/10/2016.
 */

@Agent
public class Translation2BDI {


    @AgentFeature
    IBDIAgentFeature bdi;

    @Capability(beliefmapping=@Mapping(value="wordtable"))
    TranslationCapability capability = new TranslationCapability();

    private Map<String, String> wordtable = new HashMap<String, String>();

    @AgentCreated
    public void init(){
        wordtable.put("coffee", "Kaffee");
        wordtable.put("milk", "Milch");
        wordtable.put("cow", "Kuh");
        wordtable.put("cat", "Katze");
        wordtable.put("dog", "Hund");
        wordtable.put("puppy", "Hund");
        wordtable.put("hound", "Hund");
        wordtable.put("jack", "Katze");
        wordtable.put("crummie", "Kuh");
    }

    @Plan
    protected List<String> findSynonyms(ChangeEvent ev)
    {
        String eword = (String)((Object[])ev.getValue())[0];
        List<String> ret = new ArrayList<String>();
        String gword = wordtable.get(eword);
        for(String key: wordtable.keySet())
        {
            if(wordtable.get(key).equals(gword))
            {
                ret.add(key);
            }
        }
        return ret;
    }

    @AgentBody
    private void body(){
        String eword = "coco";
        String gword = (String) bdi.dispatchTopLevelGoal(capability.new Translate2(eword)).get();

        System.out.printf("Translating %s to %s", eword, gword);
    }

}
