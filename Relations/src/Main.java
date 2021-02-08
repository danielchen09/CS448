import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Relation r = new Relation("A->BC,CD->E,B->D,E->A");
        System.out.println(r.getCandidateKeys());
    }
}
