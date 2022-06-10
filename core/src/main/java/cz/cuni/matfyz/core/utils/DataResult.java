package cz.cuni.matfyz.core.utils;

/**
 * 
 * @author jachym.bartik
 */
public class DataResult<OutputType> extends Result {
    
    public final OutputType data;

    public DataResult(OutputType data) {
        super();
        this.data = data;
    }

    public DataResult(OutputType data, String error) {
        super(error);
        this.data = data;
    }

    public DataResult(boolean condition, OutputType correctData, OutputType errorData, String error) {
        super(condition, error);
        this.data = condition ? correctData : errorData;
    }

    public DataResult(boolean condition, OutputType data, String error) {
        this(condition, data, data, error);
    }

}
