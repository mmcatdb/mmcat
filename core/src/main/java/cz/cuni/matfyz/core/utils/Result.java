package cz.cuni.matfyz.core.utils;

/**
 * 
 * @author jachym.bartik
 */
public class Result<OutputType> {
    
    public final OutputType data;
    public final String error;
    public final boolean status;

    public Result(OutputType data) {
        this.data = data;
        this.status = true;
        this.error = "";
    }

    public Result(OutputType data, String error) {
        this.data = data;
        this.status = false;
        this.error = error;
    }

}
