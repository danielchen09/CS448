package db.relational;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Attribute<T> {
    public static HashMap<String, Attribute> attributes = new HashMap<>();

    private String name;
    private String type;

    // region Constructor

    /**
     * attribute from a name, with type initialized
     * as a string
     * @param name
     */
    private Attribute(String name) {
        this.name = name;
        this.type = "String";
    }

    /**
     * attribute from a name and a type
     * @param name
     * @param type
     */
    private Attribute(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * attribute from a string like "name type" or "name"
     * @param str
     * @return
     */
    public static Attribute fromString(String str) {
        String[] strSplit = str.split(" ");
        Attribute attr = addAttribute(strSplit[0]);
        if (strSplit.length == 2)
            attr.setType(strSplit[1]);
        return attr;
    }

    /**
     * IMPORTANT: create attribute with this function
     * @param name
     * @return
     */
    private static Attribute addAttribute(String name) {
        if (!attributes.containsKey(name)) {
            attributes.put(name, new Attribute(name));
        }
        return attributes.get(name);
    }
    // endregion

    /**
     * return an attribute's closure under a functional dependency
     * set f
     * @param f
     * @return
     */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
