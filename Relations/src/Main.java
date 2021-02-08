import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FunctionalDependencySet fds = new FunctionalDependencySet("A->C,A->D,D->C");
        System.out.println(fds.canonicalCover());
    }
}
