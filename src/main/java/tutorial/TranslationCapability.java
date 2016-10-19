package tutorial;

import jadex.bdiv3.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cenas on 10/10/2016.
 */

@Capability
public class TranslationCapability {

    @Belief
    public native Map<String, String> getWordtable();

    @Belief
    public native void setWordtable(Map<String, String> wordtable);

    @Plan(trigger=@Trigger(goals=Translate2.class))
    private String translatePlan(String word){
        return getWordtable().get(word);
    }

    @Goal
    public class Translate2{

        @GoalParameter
        String eword;

        @GoalResult
        String gword;

        public Translate2(String eword){
            this.eword = eword;
        }

    }
}
