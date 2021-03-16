package db.table;

import db.relational.Attribute;
import db.relational.Relation;
import java.util.HashMap;

public class Tuple {
    private Relation relation;
    public HashMap<String, String> data;

    public Tuple(Relation relation) {
        this.relation = relation;
        for (Attribute attr : relation.attributes.toList()) {
            data.put(attr.getName(), "");
        }
    }

    public String get(String attributeName) {
        return data.get(attributeName);
    }
    public String get(Attribute attr) {
        return data.get(attr.getName());
    }

    public String[] project(String...attributeName) {
        String[] result = new String[attributeName.length];
        for (int i = 0; i < attributeName.length; i++) {
            result[i] = get(attributeName[i]);
        }
        return result;
    }

    public void set(String attributeName, String value) {
        data.put(attributeName, value);
    }
    public void set(Attribute attr, String value) {
        data.put(attr.getName(), value);
    }

    public int size() {
        return relation.attributes.size();
    }
}
