package db.relational;

import db.util.Set;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

public class AttributeSet extends Set<Attribute> {
    // region Constructors
    public AttributeSet(){}

    /**
     * attribute set with one element
     * @param attribute
     */
    public AttributeSet(Attribute attribute) {
        this.add(attribute);
    }

    /**
     * attribute set initialized by a set of attributes
     * @param attrs
     */
    public AttributeSet(Set<Attribute> attrs) {
        for (Attribute attr : attrs.toList()) {
            this.add(attr);
        }
    }

    /**
     * copy of another attribute set attrs
     * @param attrs
     */
    public AttributeSet(AttributeSet attrs) {
        for (Attribute attr : attrs.toList()) {
            this.add(attr);
        }
    }

    /**
     * attributes in a functional dependency
     * @param fd
     */
    public AttributeSet(FunctionalDependency fd) {
        AttributeSet attrs = fd.attributes();
        for (Attribute attr : attrs.toList()) {
            this.add(attr);
        }
    }

    /**
     * attribute set with one element from "name type" or "name"
     * @param attr
     */
    public AttributeSet(String attr) {
        this.add(Attribute.fromString(attr));
    }

    /**
     * attribute set with multiple elements
     * @param attribute
     */
    public AttributeSet(String... attribute) {
        for (String attr : attribute) {
            this.add(Attribute.fromString(attr));
        }
    }

    /**
     * attribute set from string separated by delimeter
     * @param attrsString
     * @param delimiter
     * @return
     */
    public static AttributeSet fromString(String attrsString, String delimiter) {
        AttributeSet attrset = new AttributeSet();
        String[] attrs = attrsString.split(delimiter);
        for (String attr : attrs) {
            attr = attr.trim();
            attrset.union(new AttributeSet(attr.replace(" ", "_")));
        }
        return attrset;
    }
    // endregion

    public AttributeSet closureUnder(FunctionalDependencySet f) {
        AttributeSet result = new AttributeSet(this);
        List<FunctionalDependency> fds = f.toList();
        int change;
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

    public List<AttributeSet> generateCombinations() {
        List<AttributeSet> allCombinations = new ArrayList<>();
        for (int i = 1; i <= this.size(); i++) {
            List<AttributeSet> combinations = generateCombinations(this.size() - 1);
            for (AttributeSet attrs : combinations) {
                allCombinations.add(attrs);
            }
        }
        return allCombinations;
    }

    public List<AttributeSet> generateCombinations(int n) {
        List<Attribute> attributes = this.toList();
        List<AttributeSet> combinations = new ArrayList<>();
        generateCombinations(combinations, attributes, new int[n], 0, attributes.size() - 1, 0);
        return combinations;
    }

    private void generateCombinations(List<AttributeSet> lists, List<Attribute> attributes, int[] data, int start, int end, int index) {
        if (index == data.length) {
            AttributeSet attrs = new AttributeSet();
            for (int i : data) {
                attrs.add(attributes.get(i));
            }
            lists.add(attrs);
        } else if (start <= end) {
            data[index] = start;
            generateCombinations(lists, attributes, data, start + 1, end, index + 1);
            generateCombinations(lists, attributes, data, start + 1, end, index);
        }
    }

    public int union(AttributeSet attrs) {
        int change = 0;
        for (Attribute attr : attrs.toList()) {
            change += this.add(attr);
        }
        return change;
    }

    public static AttributeSet union(AttributeSet...sets) {
        AttributeSet attrs = new AttributeSet();
        for (AttributeSet set : sets) {
            attrs.union(set);
        }
        return attrs;
    }

    public static AttributeSet intersect(AttributeSet s1, AttributeSet s2) {
        AttributeSet s = new AttributeSet(s1);
        for (Attribute a : s1.toList()) {
            if (!a.elementOf(s2)) {
                s.subtract(a);
            }
        }
        return s;
    }

    public static AttributeSet subtract(AttributeSet s1, AttributeSet s2) {
        return new AttributeSet(Set.subtract(s1, s2));
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
