package cz.matfyz.abstractwrappers.querycontent;

public class KindNameQuery implements QueryContent {

    public final String kindName;

    private final Integer offset;

    public int getOffset() {
        return offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    private final Integer limit;

    public int getLimit() {
        return limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public KindNameQuery(String kindName, Integer limit, Integer offset) {
        this.kindName = kindName;
        this.limit = limit;
        this.offset = offset;
    }

    public KindNameQuery(String kindName) {
        this(kindName, null, null);
    }
    
    @Override public String toString() {
        return "kindName: " + kindName + "\noffset: " + offset + "\nlimit: " + limit;
    }

}
