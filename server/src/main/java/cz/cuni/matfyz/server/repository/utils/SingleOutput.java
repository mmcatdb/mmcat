package cz.cuni.matfyz.server.repository.utils;

/**
 * 
 * @author jachym.bartik
 */
public class SingleOutput<OutputType> {

    private OutputType output = null;

    public void set(OutputType output) {
        this.output = output;
    }

    OutputType get() {
        return this.output;
    }

}
