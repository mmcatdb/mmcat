package cz.matfyz.core.rsd;

public abstract class BinaryPropertiesInHeuristics {
    public static final int NONE = 0;
    public static final int UNIQUE = (int) Math.pow(2, 0);
    public static final int REQUIRED = (int) Math.pow(2, 1);
    public static final int SEQUENTIAL = (int) Math.pow(2, 2);
}
