package cz.matfyz.abstractwrappers;

/**
 * @author jachym.bartik
 */
public class BaseControlWrapper {

    private final boolean isWritable;
    private final boolean isQueryable;

    public BaseControlWrapper(boolean isWritable, boolean isQueryable) {
        this.isWritable = isWritable;
        this.isQueryable = isQueryable;
    }

    public boolean isWritable() {
        return isWritable;
    }

    public boolean isQueryable() {
        return isQueryable;
    }

}
