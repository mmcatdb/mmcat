package cz.matfyz.server.repository.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachym.bartik
 */
public class ArrayOutput<T> {

    private List<T> output = new ArrayList<>();

    public void add(T outputItem) {
        this.output.add(outputItem);
    }

    List<T> get() {
        return this.output;
    }

}
