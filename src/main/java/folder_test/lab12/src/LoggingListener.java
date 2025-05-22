import java.util.ArrayList;
import java.util.List;

public class LoggingListener implements Observer{
    private List<Paper> paperList= new ArrayList<>();
    public void newPaper(Paper p) {
        paperList.add(p);
    }
    @Override
    public void update(int paperID) {
        Paper paper = paperList.get(paperID);
        paper.run();
    }
}
