package cz.cuni.matfyz.core.utils;

/**
 * @author jachym.bartik
 */
public class DataResult<T> extends Result {
    
    public final T data;

    public DataResult(T data) {
        super();
        this.data = data;
    }

    public DataResult(T data, String error) {
        super(error);
        this.data = data;
    }

    public DataResult(boolean condition, T correctData, T errorData, String error) {
        super(condition, error);
        this.data = condition ? correctData : errorData;
    }

    public DataResult(boolean condition, T data, String error) {
        this(condition, data, data, error);
    }

}
