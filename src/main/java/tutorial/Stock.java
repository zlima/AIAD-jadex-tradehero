package tutorial;

/**
 * Created by Cenas on 10/27/2016.
 */
public class Stock {
    private String type;
    private double close;
    private double open;
    private double high;
    private double low;
    private int shares;
    private String symbol;

    public Stock(){

    }

    public Stock(double open){
        this.open=open;
        this.type="Open";
    }

    public Stock(String symbol, double open, double close, double high, double low, int shares ){
        this.symbol = symbol;
        this.type="Close";
        this.close=close;
        this.open=open;
        this.high=high;
        this.low=low;
        this.shares=shares;
    }

    public void setValues(double open){
        this.open=open;
    }

    public String getSymbol() {
        return symbol;
    }


    public String getType() {
        return type;
    }

    public double getClose() {
        return close;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public int getShares() {
        return shares;
    }
}
