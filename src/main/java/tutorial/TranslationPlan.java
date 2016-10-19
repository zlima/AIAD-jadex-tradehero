package tutorial;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bdiv3.runtime.IPlan;

import java.util.Map;

/**
 * Created by Cenas on 10/8/2016.
 */
@Plan
public class TranslationPlan
{
    protected Map<String, String> wordtable;


    @PlanAPI
    protected IPlan plan;


    @PlanPassed
    public void passed()
    {
        System.out.println("Plan finished successfully.");
    }

    @PlanAborted
    public void aborted()
    {
        System.out.println("Plan aborted.");
    }

    @PlanFailed
    public void failed(Exception e)
    {
        System.out.println("Plan failed: "+e);
    }

    public TranslationPlan()
    {
        // Init the wordtable and add some words
    }

    @PlanBody
    public void translateEnglishGerman(ChangeEvent<Object[]> event)
    {
        // throw new PlanAbortedException();
        // Fetch some word from the table and print the translation
        System.out.println("Plan started.");
        plan.waitFor(10000).get();
        System.out.println("Plan resumed.");


    }
}