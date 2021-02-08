import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Relation {
    public Set<Attribute> attributes;
    public FunctionalDependencySet functionalDependencies;

    public Relation(String fds) {
        this.functionalDependencies = new FunctionalDependencySet(fds);
        this.attributes = this.functionalDependencies.attributes();
    }

    public Relation(FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = fds.attributes();
    }

    public Attribute getAttribute(String name) {
        return Attribute.get(name);
    }

    public Set<Set<Attribute>> getSuperKeys() {
        FunctionalDependencySet closure = closure();
        Set<Set<Attribute>> superkeys = new Set<>();
        for (FunctionalDependency fd : closure.toList()) {
            if (fd.rhs.equals(attributes)) {
                superkeys.union(fd.lhs);
            }
        }
        return superkeys;
    }

    public List<Set<Attribute>> getCandidateKeys() {
        List<Set<Attribute>> superkeys = getSuperKeys().toList();
        for (int i = superkeys.size() - 1; i >= 0; i--) {
            if (isSuperSet(superkeys, superkeys.get(i))) {
                superkeys.remove(i);
            }
        }
        return superkeys;
    }

    private boolean isSuperSet(List<Set<Attribute>> sets, Set<Attribute> attrs) {
        for (Set<Attribute> set : sets) {
            if (set.strictSubsetOf(attrs)) {
                return true;
            }
        }
        return false;
    }

    public FunctionalDependencySet closure() {
        return functionalDependencies.closure();
    }

    public Set<Attribute> closureAttribute(String name) {
        return getAttribute(name).closureUnder(functionalDependencies);
    }
}
