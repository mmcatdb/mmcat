package cz.matfyz.core.rsd;

public abstract class Char {

    public static final int TRUE = 1;  //001
    public static final int FALSE = 3; // 011
    public static final int UNKNOWN = 7; // 111


    // 001 | 011 = 011
    // ... | 111 = 111

    public static int min(int a, int b) {
        return a | b;
//        if (a == FALSE || b == FALSE) {
//            return FALSE;
//        }
//        if (a == UNKNOWN || b == UNKNOWN) {
//            return UNKNOWN;
//        }
//        return TRUE;
    }
}
