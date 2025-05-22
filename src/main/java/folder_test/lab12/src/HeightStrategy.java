import java.util.List;

public class HeightStrategy implements Strategy {
    private final List<Cube> cubeList;
    public HeightStrategy(List<Cube> cubeList) {
        this.cubeList = cubeList;
    }

    @Override
    public void strategy() {
        for(Cube cube : cubeList) {
           System.out.println("For cube "+cube.getX() +" height is " +cube.getHeight());
        }
    }
}
