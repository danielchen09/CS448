import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Relation r = new Relation("F.txt", true, "", ", ", "→");
        System.out.println(r);
        System.out.println(r.functionalDependencies);
    }
}
