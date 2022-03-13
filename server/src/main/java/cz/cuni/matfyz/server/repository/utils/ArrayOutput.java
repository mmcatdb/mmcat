package cz.cuni.matfyz.server.repository.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author jachym.bartik
 */
public class ArrayOutput<OutputType> {

    private List<OutputType> output = new ArrayList<>();

    public void add(OutputType outputItem) {
        this.output.add(outputItem);
    }

    List<OutputType> get() {
        return this.output;
    }

}
