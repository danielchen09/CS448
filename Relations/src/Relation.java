import org.w3c.dom.Attr;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Relation {
    public String name;
    public AttributeSet attributes;
    public FunctionalDependencySet functionalDependencies;

    public Relation(String fds, boolean isFile, String delimiter1, String delimiter2, String arrow) {
        if (isFile) {
            try {
                Scanner scanner = new Scanner(new File(fds));
                String relationStr = scanner.nextLine();
                Pattern pattern = Pattern.compile("(.*?)\\((.*?)\\)");
                Matcher matcher = pattern.matcher(relationStr);
                if (matcher.find() && matcher.groupCount() == 2) {
                    this.name = matcher.group(1);
                    this.attributes = new AttributeSet(matcher.group(2), ", ");
                }
                functionalDependencies = readFunctionalDependencies(scanner, delimiter2, arrow);
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            this.functionalDependencies = FunctionalDependencySet.initByString(fds, delimiter1, delimiter2, arrow);
            this.attributes = this.functionalDependencies.attributes();
        }
    }

    public Relation(FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = fds.attributes();
    }

    public Relation(AttributeSet attributes) {
        this.attributes = attributes;
    }

    public Relation(AttributeSet attributes, FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = attributes;
    }

    public Relation(Relation r) {
        this.functionalDependencies = new FunctionalDependencySet(r.functionalDependencies);
        this.attributes = new AttributeSet(r.attributes);
    }

    public static Set<Relation> readRelations(String filename, int relations, String delimiter, String arrow) {
        Set<Relation> result = new Set<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            for (int i = 0; i < relations; i++) {
                String relationStr = scanner.nextLine();
                Pattern pattern = Pattern.compile("(.*?)\\((.*?)\\)");
                Matcher matcher = pattern.matcher(relationStr);
                if (matcher.find() && matcher.groupCount() == 2) {
                    result.union(new Relation(new AttributeSet(matcher.group(2), ", ")).name(matcher.group(1)));
                }
            }
            FunctionalDependencySet fds = Relation.readFunctionalDependencies(scanner, delimiter, arrow);
            for (Relation r : result.toList()) {
                r.functionalDependencies = fds;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static FunctionalDependencySet readFunctionalDependencies (Scanner scanner, String delimiter2, String arrow) {
        FunctionalDependencySet fds = new FunctionalDependencySet();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            fds.union(FunctionalDependency.initByLine(line, delimiter2, arrow));
        }
        return fds;
    }

    public Relation name(String name) {
        this.name = name;
        return this;
    }

    public FunctionalDependencySet BCNFViolations() {
        FunctionalDependencySet violations = new FunctionalDependencySet();
        for (FunctionalDependency fd : functionalDependencies.toList()) {
            if (fd.attributes().subsetOf(attributes) && !isSuperKey(fd.lhs)) {
                violations.union(fd);
            }
        }
        return violations;
    }

    public boolean inBCNF() {
        return this.BCNFViolations().isEmpty();
    }

    public Set<Relation> BCNFDecomposition() {
        if (inBCNF()) {
            return new Set<>(this);
        }
        Set<Relation> result = new Set<>(this);
        boolean done = false;
        int i = 0;
        while (!done) {
            int changed = 0;
            List<Relation> resultList = result.toList();
            for (Relation ri : resultList) {
                FunctionalDependency violation = ri.BCNFViolations().get();
                if (violation != null) {
                    result.subtract(ri);
                    result.union(Relation.subtract(ri, violation.rhs).name("R" + (++i)));
                    result.union(new Relation(new AttributeSet(violation), ri.functionalDependencies).name("R" + (++i)));
                    changed++;
                }
            }
            done = changed == 0;
        }
        return result;
    }

    public boolean in3NF() {
        FunctionalDependencySet BCNFViolations = BCNFViolations();
        Set<AttributeSet> candidateKeys = getCandidateKeys();
        for (FunctionalDependency fd : BCNFViolations.toList()) {
            if (!containedInCandidateKey(fd)) {
                return true;
            }
        }
        return true;
    }

    private boolean containedInCandidateKey(FunctionalDependency fd) {
        Set<AttributeSet> candidateKeys = getCandidateKeys();
        for (AttributeSet candidateKey : candidateKeys.toList()) {
            if (Set.subtract(fd.rhs, fd.lhs).subsetOf(candidateKey)) {
                return true;
            }
        }
        return false;
    }

    public Set<Relation> ThreeNFDecomposition() {
        if (in3NF()) {
            return new Set<>(this);
        }
        FunctionalDependencySet f_c = functionalDependencies.canonicalCover();
        Set<AttributeSet> candidateKeys = getCandidateKeys();
        List<Relation> resultList = new ArrayList<>();
        int candidates = 0;
        for (FunctionalDependency f : f_c.toList()) {
            resultList.add(new Relation(new FunctionalDependency(f.lhs, f.rhs).toSet()));
            candidates += f.attributes().elementOf(candidateKeys) ? 1 : 0;
        }
        if (candidates == 0) {
            resultList.add(new Relation(candidateKeys.get(), functionalDependencies));
        }
        Set<Relation> result = new Set<>(resultList);
        for (int i = resultList.size() - 1; i >= 0; i--) {
            for (Relation r : result.toList()) {
                if (resultList.get(i) == r) {
                    break;
                }
                if (resultList.get(i).attributes.subsetOf(r.attributes)) {
                    resultList.remove(i);
                    break;
                }
            }
        }
        return new Set<>(resultList);
    }

    public Attribute getAttribute(String name) {
        return Attribute.get(name);
    }

    public Set<AttributeSet> getSuperKeys() {
        FunctionalDependencySet closure = closure();
        Set<AttributeSet> superkeys = new Set<>();
        for (FunctionalDependency fd : closure.toList()) {
            if (attributes.subsetOf(fd.rhs)) {
                superkeys.union(fd.lhs);
            }
        }
        return superkeys;
    }

    public Set<AttributeSet> getCandidateKeys() {
        List<AttributeSet> superkeysList = getSuperKeys().toList();
        Set<AttributeSet> superkeys = new Set<>();
        for (AttributeSet superkey : superkeysList) {
            if (!isSuperSet(superkeysList, superkey)) {
                superkeys.union(superkey);
            }
        }
        return superkeys;
    }

    public boolean isSuperKey(AttributeSet attrs) {
        return attributes.subsetOf(attrs.closureUnder(functionalDependencies));
    }

    public static boolean isSuperKey(AttributeSet attrs, Relation r) {
        return r.attributes.subsetOf(attrs.closureUnder(r.functionalDependencies));
    }

    private boolean isSuperSet(List<AttributeSet> sets, AttributeSet attrs) {
        for (AttributeSet set : sets) {
            if (set.strictSubsetOf(attrs)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLossless(Relation r1, Relation r2) {
        AttributeSet intersect = AttributeSet.intersect(r1.attributes, r2.attributes);
        return Relation.isSuperKey(intersect, r1) || Relation.isSuperKey(intersect, r2);
    }

    public FunctionalDependencySet closure() {
        return functionalDependencies.closure2(attributes);
    }

    public static Relation subtract(Relation r1, Relation r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.subtract(r1.attributes, r2.attributes);
        return r;
    }
    public static Relation subtract(Relation r1, AttributeSet r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.subtract(r1.attributes, r2);
        return r;
    }
    public static Relation subtract(Relation r1, Attribute r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.subtract(r1.attributes, r2.toSet());
        return r;
    }
    public static Relation intersect(Relation r1, Relation r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.intersect(r1.attributes, r2.attributes);
        return r;
    }

    public AttributeSet closureAttribute(String name) {
        return getAttribute(name).closureUnder(functionalDependencies);
    }

    public Set<Relation> split(String delimiter, String...rs) {
        Set<Relation> result = new Set<>();
        for (int i = 0; i < rs.length; i++) {
            result.union(new Relation(new AttributeSet(rs[i], delimiter), functionalDependencies).name("R" + (i + 1)));
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", name, attributes);
    }
}
