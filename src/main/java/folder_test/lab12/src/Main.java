import java.util.Arrays;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Strategy strategy1 = new SimulateDrawingStrategy();
        Cube cube1= new Cube(1,1.0);
        Cube cube2= new Cube(2,2.0);
        Strategy strategy2= new HeightStrategy(Arrays.asList(cube1,cube2));
        Paper paper1 = new Paper(0,strategy1);
        Paper paper2 = new Paper(1,strategy2);
        LoggingListener loggingListener = new LoggingListener();
        loggingListener.newPaper(paper1);
        loggingListener.newPaper(paper2);
        Camera camera=new Camera();
        camera.registerObserver(loggingListener);
        camera.detectPaper(0);
        camera.detectPaper(1);

    }
}