package db.table;

import java.util.Iterator;

public class Select implements Iterator<Tuple> {

    /**
     *
     * @param in
     * @param where "attrName >/</>=/<=/=/<> value"
     */
    public Select(Iterator<Tuple> in, String where) {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Tuple next() {
        return null;
    }
}
