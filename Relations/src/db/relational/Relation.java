package db.relational;

import db.util.Set;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Relation {
    public String name;
    public AttributeSet attributes;
    public FunctionalDependencySet functionalDependencies;

    // region Constructors
    public Relation() {
        this.attributes = new AttributeSet();
        this.functionalDependencies = new FunctionalDependencySet();
    }
    /**
     * relation from a functional dependency set
     * attributes of the relation are those that exist
     * in the functional dependencies
     * @param fds
     */
    public Relation(FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = fds.attributes();
    }

    /**
     * relation from an attribute set with no
     * functional dependencies
     * @param attributes
     */
    public Relation(AttributeSet attributes) {
        this.attributes = attributes;
    }

    /**
     * relation from an attribute set and functional dependency set
     * the attributes have to contain all used in fds
     * @param attributes
     * @param fds
     */
    public Relation(AttributeSet attributes, FunctionalDependencySet fds) {
        this.functionalDependencies = fds;
        this.attributes = attributes;
    }

    /**
     * copy of a relation r
     * @param r
     */
    public Relation(Relation r) {
        this.functionalDependencies = new FunctionalDependencySet(r.functionalDependencies);
        this.attributes = new AttributeSet(r.attributes);
    }

    /**
     * relation from a string "name(attr1, attr2, attr3...)"
     * @param relationStr
     * @return relation if success, null if failed
     */
    public static Relation fromString(String relationStr) {
        Pattern pattern = Pattern.compile("(.*?)\\((.*?)\\)");
        Matcher matcher = pattern.matcher(relationStr);
        if (matcher.find() && matcher.groupCount() == 2) {
            String name = matcher.group(1);
            AttributeSet attributes = AttributeSet.fromString(matcher.group(2), ",");
            return new Relation(attributes).name(name);
        }
        return null;
    }

    /**
     * relation from a file with the first line as "name(attr1, attr2, attr3...)"
     * and the following lines are functional dependencies
     * @param filename
     * @return relation if success, null if failed
     */
    public static Relation fromFile(String filename, String delimeter_nextAttr, String arrow) {
        try {
            Scanner scanner = new Scanner(new File(filename));
            Relation relation = Relation.fromString(scanner.nextLine());
            if (relation == null) {
                return null;
            }
            while (scanner.hasNextLine()) {
                relation.functionalDependencies.add(
                        FunctionalDependency.fromString(scanner.nextLine(), delimeter_nextAttr, arrow));
            }
            return relation;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    // endregion

    /**
     * usage new Relation(...).name(...)
     * @param name
     * @return
     */
    public Relation name(String name) {
        this.name = name;
        return this;
    }

    // region BCNF
    public FunctionalDependencySet BCNFViolations() {
        FunctionalDependencySet violations = new FunctionalDependencySet();
        for (FunctionalDependency fd : functionalDependencies.toList()) {
            if (fd.attributes().subsetOf(attributes) && !isSuperKey(fd.lhs)) {
                violations.add(fd);
            }
        }
        return violations;
    }

    public boolean inBCNF() {
        return this.BCNFViolations().isEmpty();
    }

    public Set<Relation> BCNFDecomposition() {
        if (inBCNF()) {
            return new Set<>(this);
        }
        Set<Relation> result = new Set<>(this);
        boolean done = false;
        int i = 0;
        while (!done) {
            int changed = 0;
            List<Relation> resultList = result.toList();
            for (Relation ri : resultList) {
                FunctionalDependency violation = ri.BCNFViolations().get();
                if (violation != null) {
                    result.subtract(ri);
                    result.add(subtract(ri, violation.rhs).name("R" + (++i)));
                    result.add(new Relation(new AttributeSet(violation), ri.functionalDependencies).name("R" + (++i)));
                    changed++;
                }
            }
            done = changed == 0;
        }
        return result;
    }
    // endregion

    // region 3NF
    public boolean in3NF() {
        FunctionalDependencySet BCNFViolations = BCNFViolations();
        Set<AttributeSet> candidateKeys = getCandidateKeys();
        for (FunctionalDependency fd : BCNFViolations.toList()) {
            if (!containedInCandidateKey(fd)) {
                return true;
            }
        }
        return true;
    }

    private boolean containedInCandidateKey(FunctionalDependency fd) {
        Set<AttributeSet> candidateKeys = getCandidateKeys();
        for (AttributeSet candidateKey : candidateKeys.toList()) {
            if (Set.subtract(fd.rhs, fd.lhs).subsetOf(candidateKey)) {
                return true;
            }
        }
        return false;
    }

    public Set<Relation> ThreeNFDecomposition() {
        if (in3NF()) {
            return new Set<>(this);
        }
        FunctionalDependencySet f_c = functionalDependencies.canonicalCover();
        Set<AttributeSet> candidateKeys = getCandidateKeys();
        List<Relation> resultList = new ArrayList<>();
        int candidates = 0;
        for (FunctionalDependency f : f_c.toList()) {
            resultList.add(new Relation(new FunctionalDependency(f.lhs, f.rhs).toSet()));
            candidates += f.attributes().elementOf(candidateKeys) ? 1 : 0;
        }
        if (candidates == 0) {
            resultList.add(new Relation(candidateKeys.get(), functionalDependencies));
        }
        Set<Relation> result = new Set<>(resultList);
        for (int i = resultList.size() - 1; i >= 0; i--) {
            for (Relation r : result.toList()) {
                if (resultList.get(i) == r) {
                    break;
                }
                if (resultList.get(i).attributes.subsetOf(r.attributes)) {
                    resultList.remove(i);
                    break;
                }
            }
        }
        return new Set<>(resultList);
    }
    // endregion

    public Attribute getAttribute(String name) {
        return Attribute.get(name);
    }

    public Set<AttributeSet> getSuperKeys() {
        FunctionalDependencySet closure = closure();
        Set<AttributeSet> superkeys = new Set<>();
        for (FunctionalDependency fd : closure.toList()) {
            if (attributes.subsetOf(fd.rhs)) {
                superkeys.add(fd.lhs);
            }
        }
        return superkeys;
    }

    public Set<AttributeSet> getCandidateKeys() {
        List<AttributeSet> superkeysList = getSuperKeys().toList();
        Set<AttributeSet> superkeys = new Set<>();
        for (AttributeSet superkey : superkeysList) {
            if (!isSuperSet(superkeysList, superkey)) {
                superkeys.add(superkey);
            }
        }
        return superkeys;
    }

    public boolean isSuperKey(AttributeSet attrs) {
        return attributes.subsetOf(attrs.closureUnder(functionalDependencies));
    }

    public static boolean isSuperKey(AttributeSet attrs, Relation r) {
        return r.attributes.subsetOf(attrs.closureUnder(r.functionalDependencies));
    }

    private boolean isSuperSet(List<AttributeSet> sets, AttributeSet attrs) {
        for (AttributeSet set : sets) {
            if (set.strictSubsetOf(attrs)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLossless(Relation r1, Relation r2) {
        AttributeSet intersect = AttributeSet.intersect(r1.attributes, r2.attributes);
        return Relation.isSuperKey(intersect, r1) || Relation.isSuperKey(intersect, r2);
    }

    public FunctionalDependencySet closure() {
        return functionalDependencies.closure2(attributes);
    }

    public static Relation subtract(Relation r1, Relation r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.subtract(r1.attributes, r2.attributes);
        return r;
    }
    public static Relation subtract(Relation r1, AttributeSet r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.subtract(r1.attributes, r2);
        return r;
    }
    public static Relation subtract(Relation r1, Attribute r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.subtract(r1.attributes, r2.toSet());
        return r;
    }
    public static Relation intersect(Relation r1, Relation r2) {
        Relation r = new Relation(r1);
        r.attributes = AttributeSet.intersect(r1.attributes, r2.attributes);
        return r;
    }

    public AttributeSet closureAttribute(String name) {
        return getAttribute(name).closureUnder(functionalDependencies);
    }

    public Set<Relation> split(String delimiter, String...rs) {
        Set<Relation> result = new Set<>();
        for (int i = 0; i < rs.length; i++) {
            result.add(new Relation(AttributeSet.fromString(rs[i], delimiter), functionalDependencies).name("R" + (i + 1)));
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", name, attributes);
    }
}
