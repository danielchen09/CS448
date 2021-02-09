import java.util.Objects;

public class FunctionalDependency {
    public AttributeSet lhs = new AttributeSet();
    public AttributeSet rhs = new AttributeSet();
    public boolean multivalued = false;

    public FunctionalDependency(AttributeSet lhs, AttributeSet rhs) {
        for (Attribute attr : lhs.toList()) {
            this.lhs.union(attr);
        }
        for (Attribute attr : rhs.toList()) {
            this.rhs.union(attr);
        }
    }

    public FunctionalDependency(String fdString, String delimiter, String arrow, boolean byLine) {
        this.initByLine(fdString, delimiter, arrow);
    }

    public FunctionalDependency(String[] lhs, String[] rhs, String delimiter) {
        for (String attr : lhs) {
            this.lhs.union(Attribute.addAttribute(attr.replace(" ", "_")));
        }
        for (String attr : rhs) {
            this.rhs.union(Attribute.addAttribute(attr.replace(" ", "_")));
        }
    }

    public FunctionalDependency multivalued(boolean multivalued) {
        this.multivalued = multivalued;
        return this;
    }

    public static FunctionalDependency initByString(String lhs, String rhs, String delimiter) {
        String[] attributes_lhs = lhs.split(delimiter);
        String[] attributes_rhs = rhs.split(delimiter);
        FunctionalDependency fd = new FunctionalDependency(attributes_lhs, attributes_rhs, delimiter);
        return fd;
    }

    public static FunctionalDependency initByLine(String fdString, String delimiter, String arrow) {
        boolean multivalued = false;
        if (fdString.contains(arrow + arrow)) {
            arrow = arrow + arrow;
            multivalued = true;
        }
        String[] fdSplit = fdString.split(arrow);
        return FunctionalDependency.initByString(fdSplit[0].trim(), fdSplit[1].trim(), delimiter).multivalued(multivalued);
    }

    public FunctionalDependency(FunctionalDependency fd) {
        for (Attribute attr : fd.lhs.toList()) {
            this.lhs.union(attr);
        }
        for (Attribute attr : fd.rhs.toList()) {
            this.rhs.union(attr);
        }
    }

    public AttributeSet attributes() {
        AttributeSet attrs = new AttributeSet();
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
            return new FunctionalDependency(fd1.lhs, AttributeSet.union(fd1.rhs, fd2.rhs));
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
        return lhs.toString() + "->" + (multivalued ? ">" : "") + rhs.toString();
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
