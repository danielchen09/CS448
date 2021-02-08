import java.util.Objects;

public class FunctionalDependency {
    public Set<Attribute> lhs = new Set<>();
    public Set<Attribute> rhs = new Set<>();

    public FunctionalDependency(Set<Attribute> lhs, Set<Attribute> rhs) {
        for (Attribute attr : lhs.toList()) {
            this.lhs.union(attr);
        }
        for (Attribute attr : rhs.toList()) {
            this.rhs.union(attr);
        }
    }

    public FunctionalDependency(String lhs, String rhs, String delimiter) {
        String[] attributes_lhs = lhs.split(delimiter);
        String[] attributes_rhs = rhs.split(delimiter);

        for (String attr : attributes_lhs) {
            this.lhs.union(Attribute.addAttribute(attr));
        }
        for (String attr : attributes_rhs) {
            this.rhs.union(Attribute.addAttribute(attr));
        }
    }

    public FunctionalDependency(FunctionalDependency fd) {
        for (Attribute attr : fd.lhs.toList()) {
            this.lhs.union(attr);
        }
        for (Attribute attr : fd.rhs.toList()) {
            this.rhs.union(attr);
        }
    }

    public Set<Attribute> attributes() {
        Set<Attribute> attrs = new Set<>();
        for (Attribute attr : this.lhs.toList()) {
            attrs.union(attr);
        }
        for (Attribute attr : this.rhs.toList()) {
            attrs.union(attr);
        }
        return attrs;
    }

    public static FunctionalDependency augmentationRule(FunctionalDependency original, Attribute attr) {
        FunctionalDependency fd = new FunctionalDependency(original);
        fd.lhs.union(attr);
        fd.rhs.union(attr);
        return fd;
    }

    public static FunctionalDependency transitivityRule(FunctionalDependency fd1, FunctionalDependency fd2) {
        if (fd2.lhs.subsetOf(fd1.rhs)) {
            return new FunctionalDependency(fd1.lhs, (Set<Attribute>) Set.union(fd1.rhs, fd2.rhs));
        }
        return null;
    }

    public FunctionalDependencySet toSet() {
        FunctionalDependencySet fds = new FunctionalDependencySet();
        fds.union(this);
        return fds;
    }

    @Override
    public String toString() {
        return lhs.toString() + "->" + rhs.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionalDependency that = (FunctionalDependency) o;
        return Objects.equals(lhs, that.lhs) && Objects.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs);
    }
}
