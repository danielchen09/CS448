import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FunctionalDependencySet extends Set<FunctionalDependency> {
    public FunctionalDependencySet() {}
    public FunctionalDependencySet(String fdsString) {
        //AB->C,A->BC...
        String[] fds = fdsString.split(",");
        for (String fd : fds) {
            String[] fdSplit = fd.split("->");
            this.union(new FunctionalDependency(fdSplit[0], fdSplit[1], ""));
        }
    }

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

    public Set<Attribute> attributes() {
        Set<Attribute> attributes = new Set<>();
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

    public Set<Attribute> extraneousLHS(FunctionalDependency fd) {
        // gamma = a - {A}, check if gamma -> B can be inferred from F
        Set<Attribute> attrs = fd.lhs;
        Set<Attribute> extraneous = new Set<>();
        for (Attribute A : attrs.toList()) {
            Set<Attribute> gamma = Set.remove(fd.lhs, A);
            Set<Attribute> gammaClosure = Attribute.closureUnder(gamma, this);

            if (fd.rhs.subsetOf(gammaClosure)) {
                extraneous.union(A);
            }
        }
        return extraneous;
    }

    public Set<Attribute> extraneousRHS(FunctionalDependency fd) {
        // F' = (F - {a -> b}) u (a -> {b - A})
        Set<Attribute> attrs = fd.rhs;
        Set<Attribute> extraneous = new Set<>();
        for (Attribute A : attrs.toList()) {
            FunctionalDependencySet left = FunctionalDependencySet.subtract(this, fd.toSet());
            FunctionalDependencySet right = new FunctionalDependency(fd.lhs, (Set<Attribute>) Set.remove(fd.rhs, A)).toSet();
            FunctionalDependencySet fPrime = FunctionalDependencySet.union(left, right);

            Set<Attribute> lhsClosure = Attribute.closureUnder(fd.lhs, fPrime);
            if (A.toSet().subsetOf(lhsClosure)) {
                extraneous.union(A);
            }
        }
        return extraneous;
    }

    public FunctionalDependencySet closure() {
        FunctionalDependencySet closure = new FunctionalDependencySet(this);
        int change = 0;
        Set<Attribute> attributes = attributes();
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
        int change = 0;
        do {
            FunctionalDependencySet fc_new = new FunctionalDependencySet();
            HashMap<Set<Attribute>, Set<Attribute>> lrhs = new HashMap<>();
            for (FunctionalDependency fd : fc.toList()) {
                if (!lrhs.containsKey(fd.lhs)) {
                    lrhs.put(fd.lhs, new Set<>());
                }
                lrhs.get(fd.lhs).union(fd.rhs);
            }
            for (Map.Entry<Set<Attribute>, Set<Attribute>> entry : lrhs.entrySet()) {
                fc_new.union(new FunctionalDependency(entry.getKey(), entry.getValue()));
            }
            fc = fc_new;
            for (FunctionalDependency fd : fc.toList()) {
                Set<Attribute> extraneousLHS = fc.extraneousLHS(fd);
                Set<Attribute> extraneousRHS = fc.extraneousRHS(fd);
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
        FunctionalDependency fd = new FunctionalDependency(fdSplit[0], fdSplit[1], "");
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
        for (FunctionalDependency fd : this.toList()) {
            out.append(fd + "\n");
        }
        return out.toString();
    }
}
