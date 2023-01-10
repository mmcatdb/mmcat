package cz.cuni.matfyz.server.repository.utils;

/**
 * @author jachym.bartik
 */
public class SingleOutput<T> {

    private T output = null;
    private boolean isEmpty = true;

    public void set(T output) {
        this.output = output;
        this.isEmpty = false;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    T get() {
        return this.output;
    }

}
