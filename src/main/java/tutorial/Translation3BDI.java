package tutorial;

import jadex.bdiv3.IBDIAgent;
import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.impl.PlanFailureException;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cenas on 10/12/2016.
 */
@Agent
@Service
@ProvidedServices(@ProvidedService(name="transser", type=ITranslationService.class,
        implementation=@Implementation(IBDIAgent.class)))
public class Translation3BDI {

    @AgentFeature
    protected IBDIAgentFeature bdiFeature;

   private Map<String,String> wordtable;

    @AgentCreated
    public void init(){
        wordtable = new HashMap<String, String>();

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



    @Plan(trigger=@Trigger(service=@ServiceTrigger(type=ITranslationService.class)))
    public class TranslatePlan
    {
        @PlanPrecondition
        public boolean checkPrecondition(Object[] params)
        {
            return wordtable.containsKey(params[0]);
        }

        @PlanBody
        public String body(Object[] params)
        {
            String eword = (String)params[0];
            String gword = wordtable.get(eword);
            System.out.println("Translated with internal dictionary dictionary: "+eword+" - "+gword);
            return gword;
        }


    }

    @Plan(trigger=@Trigger(service=@ServiceTrigger(type=ITranslationService.class)))
    public String internetTranslate(Object[] params)
    {
        String eword = (String)params[0];
        String ret = null;
        try
        {
            URL dict = new URL("http://wolfram.schneider.org/dict/dict.cgi?query="+eword);
            System.out.println("Following translations were found online at: "+dict);
            BufferedReader in = new BufferedReader(new InputStreamReader(dict.openStream()));
            String inline;
            while((inline = in.readLine())!=null)
            {
                if(inline.indexOf("<td>")!=-1 && inline.indexOf(eword)!=-1)
                {
                    try
                    {
                        int start = inline.indexOf("<td>")+4;
                        int end = inline.indexOf("</td", start);
                        String worda = inline.substring(start, end);
                        start = inline.indexOf("<td", start);
                        start = inline.indexOf(">", start);
                        end = inline.indexOf("</td", start);
                        String wordb = inline.substring(start, end==-1? inline.length()-1: end);
                        wordb = wordb.replaceAll("<b>", "");
                        wordb = wordb.replaceAll("</b>", "");
                        ret = worda;
                        System.out.println("Translated with internet dictionary: "+worda+" - "+wordb);
                    }
                    catch(Exception e)
                    {
                        System.out.println(inline);
                    }
                }
            }
            in.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new PlanFailureException(e.getMessage());
        }
        return ret;
    }


}
