public class Paper {
    private Strategy strategy;
    private int id;
    public Paper(int id,Strategy strategy) {
        this.id=id;
        this.strategy = strategy;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Strategy getStrategy() {
        return strategy;
    }
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
    public void run(){
        strategy.strategy();
    }
}
