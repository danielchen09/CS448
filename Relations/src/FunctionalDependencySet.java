import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;

public class FunctionalDependencySet extends Set<FunctionalDependency> {
    public FunctionalDependencySet() {}

    public FunctionalDependencySet(List<FunctionalDependency> fds) {
        for (FunctionalDependency fd : fds) {
            this.union(fd);
        }
    }

    public FunctionalDependencySet(FunctionalDependencySet fds) {
        for (FunctionalDependency fd : fds.toList()) {
            this.union(fd);
        }
    }

    public FunctionalDependencySet(Set set) {
        for (Object item : set.toList()) {
            this.union((FunctionalDependency) item);
        }
    }

    public static FunctionalDependencySet initByFile(String filename, String delimiter, String arrow) {
        FunctionalDependencySet fds = new FunctionalDependencySet();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNext()) {
                String[] fdSplit = scanner.nextLine().split(arrow);
                fds.union(FunctionalDependency.initByLine(scanner.nextLine(), delimiter, arrow));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fds;
    }

    public static FunctionalDependencySet initByString(String fdsString, String delimiter1, String delimiter2, String arrow) {
        FunctionalDependencySet result = new FunctionalDependencySet();
        //A[d2]B->C[d1]A->B[d2]C...
        String[] fds = fdsString.split(delimiter1);
        for (String fd : fds) {
            boolean multivalued = false;
            if (fd.indexOf(arrow + arrow) != -1) {
                arrow = arrow + arrow;
                multivalued = true;
            }
            String[] fdSplit = fd.split(arrow);
            result.union(FunctionalDependency.initByString(fdSplit[0].trim(), fdSplit[1].trim(), delimiter2).multivalued(multivalued));
        }
        return result;
    }

    public AttributeSet attributes() {
        AttributeSet attributes = new AttributeSet();
        for (FunctionalDependency fd : this.toList()) {
            attributes.union(fd.attributes());
        }
        return attributes;
    }

    public List<FunctionalDependency[]> getPairs() {
        List<FunctionalDependency> fds = this.toList();
        List<FunctionalDependency[]> pairs = new ArrayList<>();
        for (int i = 0; i < fds.size() - 1; i++) {
            for (int j = i + 1; j < fds.size(); j++) {
                pairs.add(new FunctionalDependency[] {fds.get(i), fds.get(j)});
                pairs.add(new FunctionalDependency[] {fds.get(j), fds.get(i)});
            }
        }
        return pairs;
    }

    public AttributeSet extraneousLHS(FunctionalDependency fd) {
        // gamma = a - {A}, check if gamma -> B can be inferred from F
        AttributeSet attrs = fd.lhs;
        AttributeSet extraneous = new AttributeSet();
        for (Attribute A : attrs.toList()) {
            AttributeSet gamma = AttributeSet.subtract(fd.lhs, A.toSet());
            AttributeSet gammaClosure = gamma.closureUnder(this);

            if (fd.rhs.subsetOf(gammaClosure)) {
                extraneous.union(A);
            }
        }
        return extraneous;
    }

    public AttributeSet extraneousRHS(FunctionalDependency fd) {
        // F' = (F - {a -> b}) u (a -> {b - A})
        AttributeSet attrs = fd.rhs;
        AttributeSet extraneous = new AttributeSet();
        for (Attribute A : attrs.toList()) {
            FunctionalDependencySet left = FunctionalDependencySet.subtract(this, fd.toSet());
            FunctionalDependencySet right = new FunctionalDependency(fd.lhs, AttributeSet.subtract(fd.rhs, A.toSet())).toSet();
            FunctionalDependencySet fPrime = FunctionalDependencySet.union(left, right);

            AttributeSet lhsClosure = fd.lhs.closureUnder(fPrime);
            if (A.toSet().subsetOf(lhsClosure)) {
                extraneous.union(A);
            }
        }
        return extraneous;
    }

    public FunctionalDependencySet closureOfN(AttributeSet attributes, int n) {
        List<AttributeSet> combinations = attributes.generateCombinations(n);
        FunctionalDependencySet closure = new FunctionalDependencySet();
        for (AttributeSet attrs : combinations) {
            closure.union(new FunctionalDependency(attrs, attrs.closureUnder(this)));
        }
        return closure;
    }

    public FunctionalDependencySet closure2(AttributeSet attributes) {
        FunctionalDependencySet fds = new FunctionalDependencySet();
        for (int i = 1; i <= attributes.size(); i++) {
            fds.union(closureOfN(attributes, i));
        }
        return fds;
    }

    public FunctionalDependencySet closure(AttributeSet attributes) {
        FunctionalDependencySet closure = new FunctionalDependencySet(this);
        int change = 0;
        do {
            change = 0;
            List<FunctionalDependency> fds = new ArrayList<>(closure.toList());
            for (FunctionalDependency f : fds) {
                for (Attribute attr : attributes.toList()) {
                    change += closure.union(FunctionalDependency.augmentationRule(f, attr));
                }
            }
            List<FunctionalDependency[]> pairs = closure.getPairs();
            for (FunctionalDependency[] pair : pairs) {
                change += closure.union(FunctionalDependency.transitivityRule(pair[0], pair[1]));
            }
        } while (change != 0);
        return closure;
    }

    public FunctionalDependencySet canonicalCover() {
        FunctionalDependencySet fc = new FunctionalDependencySet(this);
        int change;
        do {
            change = 0;
            FunctionalDependencySet fc_new = new FunctionalDependencySet();
            HashMap<AttributeSet, AttributeSet> lrhs = new HashMap<>();
            for (FunctionalDependency fd : fc.toList()) {
                if (!lrhs.containsKey(fd.lhs)) {
                    lrhs.put(fd.lhs, new AttributeSet());
                }
                lrhs.get(fd.lhs).union(fd.rhs);
            }
            for (Map.Entry<AttributeSet, AttributeSet> entry : lrhs.entrySet()) {
                fc_new.union(new FunctionalDependency(entry.getKey(), entry.getValue()));
            }
            change += fc.size() - fc_new.size();
            fc = fc_new;
            List<FunctionalDependency> fds = fc.toList();
            for (int i = fds.size() - 1; i >= 0; i--) {
                FunctionalDependency fd = fds.get(i);
                AttributeSet extraneousLHS = fc.extraneousLHS(fd);
                AttributeSet extraneousRHS = fc.extraneousRHS(fd);
                change += fd.lhs.subtract(extraneousLHS);
                change += fd.rhs.subtract(extraneousRHS);
                if (fd.attributes().isEmpty()) {
                    fc.subtract(fd);
                }
            }
        } while (change != 0);
        return fc;
    }

    public static FunctionalDependencySet subtract(FunctionalDependencySet fds1, FunctionalDependencySet fds2) {
        return new FunctionalDependencySet(Set.subtract(fds1, fds2));
    }

    public static FunctionalDependencySet union(FunctionalDependencySet...fds) {
        return new FunctionalDependencySet(Set.union(fds));
    }

    public FunctionalDependency find(String fdString) {
        String[] fdSplit = fdString.split("->");
        FunctionalDependency fd = FunctionalDependency.initByString(fdSplit[0], fdSplit[1], "");
        for (FunctionalDependency f : this.toList()) {
            if (fd.equals(f)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        List<FunctionalDependency> fds = this.toList();
        fds.sort(new Comparator<FunctionalDependency>() {
            @Override
            public int compare(FunctionalDependency f1, FunctionalDependency f2) {
                if (f1.lhs.size() > f2.lhs.size()) {
                    return 1;
                } else if (f1.lhs.size() < f2.lhs.size()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        for (FunctionalDependency fd : fds) {
            out.append(fd + "\n");
        }
        return out.toString();
    }
}
