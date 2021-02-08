import java.util.ArrayList;
import java.util.List;

public class FunctionalDependencySet extends Set<FunctionalDependency> {
    public FunctionalDependencySet(String fdsString) {
        //AB->C,A->BC...
        String[] fds = fdsString.split(",");
        for (String fd : fds) {
            String[] fdSplit = fd.split("->");
            this.union(new FunctionalDependency(fdSplit[0], fdSplit[1], ""));
        }
    }

    public FunctionalDependencySet(List<FunctionalDependency> fds) {
        for (FunctionalDependency fd : fds) {
            this.union(fd);
        }
    }

    public FunctionalDependencySet(FunctionalDependencySet fds) {
        for (FunctionalDependency fd : fds.toList()) {
            this.union(fd);
        }
    }

    public Set<Attribute> attributes() {
        Set<Attribute> attributes = new Set<>();
        for (FunctionalDependency fd : this.toList()) {
            attributes.union(fd.attributes());
        }
        return attributes;
    }

    public List<FunctionalDependency[]> getPairs() {
        List<FunctionalDependency> fds = this.toList();
        List<FunctionalDependency[]> pairs = new ArrayList<>();
        for (int i = 0; i < fds.size() - 1; i++) {
            for (int j = i + 1; j < fds.size(); j++) {
                pairs.add(new FunctionalDependency[] {fds.get(i), fds.get(j)});
                pairs.add(new FunctionalDependency[] {fds.get(j), fds.get(i)});
            }
        }
        return pairs;
    }

    public FunctionalDependencySet closure() {
        FunctionalDependencySet closure = new FunctionalDependencySet(this);
        int change = 0;
        Set<Attribute> attributes = attributes();
        do {
            change = 0;
            List<FunctionalDependency> fds = new ArrayList<>(closure.toList());
            for (FunctionalDependency f : fds) {
                for (Attribute attr : attributes.toList()) {
                    change += closure.union(FunctionalDependency.augmentationRule(f, attr));
                }
            }
            List<FunctionalDependency[]> pairs = closure.getPairs();
            for (FunctionalDependency[] pair : pairs) {
                change += closure.union(FunctionalDependency.transitivityRule(pair[0], pair[1]));
            }
        } while (change != 0);
        return closure;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (FunctionalDependency fd : this.toList()) {
            out.append(fd + "\n");
        }
        return out.toString();
    }
}
