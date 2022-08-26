package cz.cuni.matfyz.core.utils;

/**
 * @author jachym.bartik
 */
public class Result {
    
    public final String error;
    public final boolean status;

    public Result() {
        this.status = true;
        this.error = "";
    }

    public Result(String error) {
        this.status = false;
        this.error = error;
    }

    public Result(boolean condition, String error) {
        this.status = condition;
        this.error = condition ? "" : error;
    }

}
