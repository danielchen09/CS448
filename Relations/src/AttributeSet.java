import java.util.ArrayList;
import java.util.List;

public class AttributeSet extends Set<Attribute> {
    public AttributeSet(){}
    public AttributeSet(Attribute attribute) {
        this.union(attribute);
    }

    public AttributeSet(String attrsString, String delimiter) {
        String[] attrs = attrsString.split(delimiter);
        for (String attr : attrs) {
            this.union(new AttributeSet(attr.replace(" ", "_")));
        }
    }

    public AttributeSet(Set<Attribute> attrs) {
        for (Attribute attr : attrs.toList()) {
            this.union(attr);
        }
    }

    public AttributeSet(AttributeSet attrs) {
        for (Attribute attr : attrs.toList()) {
            this.union(attr);
        }
    }

    public AttributeSet(FunctionalDependency fd) {
        AttributeSet attrs = fd.attributes();
        for (Attribute attr : attrs.toList()) {
            this.union(attr);
        }
    }

    public AttributeSet(String attribute) {
        this.union(Attribute.addAttribute(attribute));
    }

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
                attrs.union(attributes.get(i));
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
            change += this.union(attr);
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
