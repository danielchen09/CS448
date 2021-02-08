import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FunctionalDependencySet fds = new FunctionalDependencySet("AB->C,A->D,D->C");
        System.out.println(fds);
        System.out.println(fds.extraneousLHS(fds.find("A->D")));
    }
}
