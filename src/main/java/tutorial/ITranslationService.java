package tutorial;

import jadex.commons.future.IFuture;

/**
 * Created by Cenas on 10/12/2016.
 */
public interface ITranslationService {
    public IFuture<String> translateEnglishGerman(String eword);
}
