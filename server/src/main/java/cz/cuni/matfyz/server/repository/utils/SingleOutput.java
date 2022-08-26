package cz.cuni.matfyz.server.repository.utils;

/**
 * @author jachym.bartik
 */
public class SingleOutput<T> {

    private T output = null;

    public void set(T output) {
        this.output = output;
    }

    T get() {
        return this.output;
    }

}
