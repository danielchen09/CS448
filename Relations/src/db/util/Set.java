package db.util;

import java.util.*;

public class Set<T> {
    private java.util.Set<T> set = new HashSet<>();

    public Set() {
    }

    public Set(T item) {
        this.set.add(item);
    }

    public Set(Set set) {
        this.union(set);
    }

    public Set(java.util.Set set) {
        this.set = new HashSet<>(set);
    }

    public Set(List<T> list) {
        for (T item : list) {
            set.add(item);
        }
    }

    public int union(Set set) {
        int added = 0;
        for (Object item : set.toList()) {
            added += this.add((T)item);
        }
        return added;
    }

    public int add(T item) {
        if (item == null) {
            return 0;
        }
        return set.add(item) ? 1 : 0;
    }

    public static Set union(Set...sets) {
        Set s = new Set();
        for (Set set : sets) {
            s.union(set);
        }
        return s;
    }

    public static Set intersect(Set s1, Set s2) {
        Set s = new Set();
        for (Object item : s1.toList()) {
            if (s2.set.contains(item)) {
                s.add(item);
            }
        }
        return s;
    }

    public int subtract(T item) {
        return this.set.remove(item) ? 1: 0;
    }

    public int subtract(Set<T> s) {
        int change = 0;
        if (s.size() == 0) {
            return change;
        }
        for (T item : s.toList()) {
            change += this.subtract(item);
        }
        return change;
    }

    public static Set subtract(Set s1, Set s2) {
        Set s = new Set(s1);
        for (Object item : s2.toList()) {
            s.subtract(item);
        }
        return s;
    }

    public static Set remove(Set s1, Object item) {
        Set s = new Set(s1);
        s.set.remove(item);
        return s;
    }

    public boolean elementOf(Set<? extends Set<T>> set) {
        for (Set<T> s : set.toList()) {
            if (s.equals(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean subsetOf(Set<T> set) {
        return set.set.containsAll(this.set);
    }

    public boolean strictSubsetOf(Set set) {
        return set.set.containsAll(this.set) && this.set.size() < set.set.size();
    }

    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    public int size() {
        return this.set.size();
    }

    public List<T> toList() {
        return new ArrayList<T>(Arrays.asList((T[]) set.toArray()));
    }

    public Set<T> clone(){
        return new Set<>(this.set);
    }

    public T get() {
        if (this.size() == 0) {
            return null;
        }
        return this.toList().get(0);
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
