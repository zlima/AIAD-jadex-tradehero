package tutorial.Services;

import jadex.commons.future.IFuture;
import java.util.ArrayList;
import java.util.HashMap;

public interface UpdateMarketService {
    public IFuture<Void> UpdateMarketService(ArrayList<HashMap> quote);
}