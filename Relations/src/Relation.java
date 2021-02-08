import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Relation {
    public Set<Attribute> attributes;
    public FunctionalDependencySet functionalDependencies;
    public Set<Set<Attribute>> candidateKeys;

    public Relation(String fds) {
        this.functionalDependencies = new FunctionalDependencySet(fds);
        this.attributes = this.functionalDependencies.attributes();
        init();
    }

    public Relation(FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = fds.attributes();
        init();
    }

    public Relation(Set<Attribute> attributes, FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = attributes;
        init();
    }

    public Relation(Relation r) {
        this.functionalDependencies = new FunctionalDependencySet(r.functionalDependencies);
        this.attributes = r.attributes.clone();
        init();
    }

    public void init() {
        this.candidateKeys = getCandidateKeys();
    }

    public boolean inBCNF() {
        for (FunctionalDependency fd : functionalDependencies.toList()) {
            if (fd.attributes().subsetOf(attributes) && !fd.lhs.subsetOf(candidateKeys)) {
                return false;
            }
        }
        return true;
    }

//    public static List<Relation> BCNFDecomposition(Relation r) {
//        Set<Relation> result = new Set<>(r);
//        boolean done = false;
//        while (!done) {
//
//        }
//    }

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

    public Set<Set<Attribute>> getCandidateKeys() {
        List<Set<Attribute>> superkeysList = getSuperKeys().toList();
        Set<Set<Attribute>> superkeys = new Set<>();
        for (Set<Attribute> superkey : superkeysList) {
            if (!isSuperSet(superkeysList, superkey)) {
                superkeys.union(superkey);
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

    public static Relation subtract(Relation r1, Relation r2) {
        Relation r = new Relation(r1);
        r.attributes = Set.subtract(r1.attributes, r2.attributes);
        return r;
    }

    public Set<Attribute> closureAttribute(String name) {
        return getAttribute(name).closureUnder(functionalDependencies);
    }
}
