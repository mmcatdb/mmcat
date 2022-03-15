package cz.cuni.matfyz.server.repository.utils;

/**
 * 
 * @author jachym.bartik
 */
public abstract class Utils {

    public static Integer getIntOrNull(int input) {
        return input == 0 ? null : input;
    }

}
