import java.util.*;

public class Set<T> {
    private java.util.Set<T> set = new HashSet<>();;

    public Set() {
    }

    public Set(T item) {
        this.set.add(item);
    }

    public int union(Set set) {
        int added = 0;
        for (Object item : set.toList()) {
            added += this.union((T)item);
        }
        return added;
    }

    public int union(T item) {
        if (item == null) {
            return 0;
        }
        return set.add(item) ? 1 : 0;
    }

    public static Set union(Set s1, Set s2) {
        Set s = new Set<>();
        s.union(s1);
        s.union(s2);
        return s;
    }

    public boolean subsetOf(Set set) {
        return set.set.containsAll(this.set);
    }

    public boolean strictSubsetOf(Set set) {
        return set.set.containsAll(this.set) && this.set.size() < set.set.size();
    }

    public List<T> toList() {
        return new ArrayList<T>(Arrays.asList((T[]) set.toArray()));
    }

    @Override
    public String toString() {
        return set.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Set<?> set1 = (Set<?>) o;
        return Objects.equals(set, set1.set);
    }

    @Override
    public int hashCode() {
        return Objects.hash(set);
    }
}
