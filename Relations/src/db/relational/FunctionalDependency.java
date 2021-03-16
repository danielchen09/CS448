package db.relational;

import java.util.Objects;

public class FunctionalDependency {
    public AttributeSet lhs = new AttributeSet();
    public AttributeSet rhs = new AttributeSet();
    public boolean multivalued = false;

    // region Constructors

    /**
     * functional dependency from left and right hand side
     * @param lhs
     * @param rhs
     */
    public FunctionalDependency(AttributeSet lhs, AttributeSet rhs) {
        for (Attribute attr : lhs.toList()) {
            this.lhs.add(attr);
        }
        for (Attribute attr : rhs.toList()) {
            this.rhs.add(attr);
        }
    }

    /**
     * functional dependency from an unseparated left and right hand side
     * ex. new FunctionalDependency("A,B,cat", "dog", ",")
     * @param lhs
     * @param rhs
     * @param delimiter_nextAttr
     */
    public FunctionalDependency(String lhs, String rhs, String delimiter_nextAttr) {
        this(lhs.split(delimiter_nextAttr), rhs.split(delimiter_nextAttr));
    }

    /**
     * functional dependency from a separated left and right hand side
     * ex. new FunctionalDependency(["a", "b", "cat"], ["dog"])
     * @param lhs
     * @param rhs
     */
    public FunctionalDependency(String[] lhs, String[] rhs) {
        for (String attr : lhs) {
            this.lhs.add(Attribute.fromString(attr.replace(" ", "_")));
        }
        for (String attr : rhs) {
            this.rhs.add(Attribute.fromString(attr.replace(" ", "_")));
        }
    }

    /**
     * copy of another function dependency fd
     * @param fd
     */
    public FunctionalDependency(FunctionalDependency fd) {
        for (Attribute attr : fd.lhs.toList()) {
            this.lhs.add(attr);
        }
        for (Attribute attr : fd.rhs.toList()) {
            this.rhs.add(attr);
        }
    }

    /**
     * functional dependency from a string "a<delimeter_nextAttr>b<arrow>c"
     * ex. a,b->c (delimeter_nextAttr = ",", arrow = "->")
     * @param str
     * @param delimeter_nextAttr
     * @param arrow
     * @return
     */
    public static FunctionalDependency fromString(String str,
                                                  String delimeter_nextAttr,
                                                  String arrow) {
        String[] lrhs = str.split(arrow);
        return new FunctionalDependency(lrhs[0], lrhs[1], delimeter_nextAttr);
    }
    // endregion

    public FunctionalDependency multivalued(boolean multivalued) {
        this.multivalued = multivalued;
        return this;
    }

    public AttributeSet attributes() {
        AttributeSet attrs = new AttributeSet();
        for (Attribute attr : this.lhs.toList()) {
            attrs.add(attr);
        }
        for (Attribute attr : this.rhs.toList()) {
            attrs.add(attr);
        }
        return attrs;
    }

    public static FunctionalDependency augmentationRule(FunctionalDependency original, Attribute attr) {
        FunctionalDependency fd = new FunctionalDependency(original);
        fd.lhs.add(attr);
        fd.rhs.add(attr);
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
        fds.add(this);
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
