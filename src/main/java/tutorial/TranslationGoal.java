package tutorial;

import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.GoalResult;

/**
 * Created by Cenas on 10/13/2016.
 */
@Goal
public class TranslationGoal
{
    @GoalResult
    protected String gword;

    @GoalParameter
    protected String eword;

    public TranslationGoal(String eword){
        this.eword = eword;
    }
}
