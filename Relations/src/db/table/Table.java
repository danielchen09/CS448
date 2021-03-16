package db.table;

import db.relational.Attribute;
import db.relational.Relation;
import org.w3c.dom.Attr;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Table {
    private Relation relation;
    private List<Tuple> tuples = new ArrayList<>();
    private List<Attribute> attributeList = new ArrayList<>();

    // region Constructors
    public Table() {
        relation = new Relation();
        tuples = new ArrayList<>();
    }

    /**
     * table from a list of attributes "name type" or "name"
     * @param attrs
     */
    public Table(String... attrs) {
        relation = new Relation();
        for (String attr : attrs) {
            addAttribute(attr);
        }
    }

    /**
     * table from a file of attributes "name type" or "name"
     * one on each line
     * @param filename
     * @return
     */
    public static Table fromFile(String filename) {
        try {
            Scanner scanner = new Scanner(new File(filename));
            Table t = new Table();
            while (scanner.hasNextLine()) {
                t.addAttribute(scanner.nextLine());
            }
            return t;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    // endregion

    /**
     * insert in the form of ["attributeName:data", ...]
     * @param data
     * @throws Exception
     */
    public void insert(String... data) throws Exception {
        if (data.length > relation.attributes.size()) {
            throw new Exception("too many attributes");
        }
        Tuple row = new Tuple(relation);
        for (String attrStr : data) {
            String[] attrStrSplit = attrStr.split(".");
            row.set(attrStrSplit[0], attrStrSplit[1]);
        }
    }

    private Iterator<Tuple> relation_open() {
        return tuples.iterator();
    }

    private void addAttribute(String attribute) {
        Attribute attr = Attribute.fromString(attribute);
        this.relation.attributes.add(attr);
        this.attributeList.add(attr);
    }
}
