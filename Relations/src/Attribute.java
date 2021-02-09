import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Attribute {
    public static HashMap<String, Attribute> attributes = new HashMap<>();

    private String name;
    public Attribute(String name) {
        this.name = name;
    }

    public static Attribute addAttribute(String name) {
        if (!attributes.containsKey(name)) {
            attributes.put(name, new Attribute(name));
        }
        return attributes.get(name);
    }

    public AttributeSet closureUnder(FunctionalDependencySet f) {
        return this.toSet().closureUnder(f);
    }

    public static AttributeSet closureUnder(AttributeSet attrs, FunctionalDependencySet f) {
        AttributeSet result = new AttributeSet(attrs);
        List<FunctionalDependency> fds = f.toList();
        int change = 0;
        do {
            change = 0;
            for (FunctionalDependency fd : fds) {
                if (fd.lhs.subsetOf(result)) {
                    change += result.union(fd.rhs);
                }
            }
        } while (change != 0);
        return result;
    }

    public boolean elementOf(AttributeSet set) {
        for (Attribute attr : set.toList()) {
            if (attr.equals(this)) {
                return true;
            }
        }
        return false;
    }

    public static Attribute get(String name) {
        return attributes.get(name);
    }

    public AttributeSet toSet() {
        return new AttributeSet(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return Objects.equals(name, attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
