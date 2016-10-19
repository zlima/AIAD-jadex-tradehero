package tutorial;

import jadex.bdiv3.annotation.Goal;

/**
 * Created by Cenas on 10/9/2016.
 */
@Goal
public class Translate
{
    protected String eword;

    protected String gword;

    public Translate(String eword)
    {
        this.eword = eword;
    }

    public String getEWord()
    {
        return eword;
    }

    public String getGWord()
    {
        return gword;
    }

    public void setGWord(String gword)
    {
        this.gword = gword;
    }
}