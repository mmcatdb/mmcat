package cz.matfyz.abstractwrappers.querycontent;

import org.checkerframework.checker.nullness.qual.Nullable;

public class KindNameQuery implements QueryContent {

    public final String kindName;

    private final @Nullable Integer offset;

    public int getOffset() {
        return offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    private final @Nullable Integer limit;

    public int getLimit() {
        return limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public KindNameQuery(String kindName, @Nullable Integer limit, @Nullable Integer offset) {
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
