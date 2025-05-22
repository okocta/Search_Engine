import java.util.ArrayList;
import java.util.List;

public class Camera{
    private List<Observer> observerList =new ArrayList<>();
    public void registerObserver(Observer observer){
        observerList.add(observer);
    }
    public void detectPaper(int paperID){
        System.out.println("Detected paper"+paperID);
        for(Observer observer : observerList){
            observer.update(paperID);
        }
    }

}
