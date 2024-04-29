package cz.matfyz.server.repository.utils;

public class BooleanOutput {

    private boolean output = false;

    public void set(boolean output) {
        this.output = output;
    }

    public void setTrue() {
        this.set(true);
    }

    public void setFalse() {
        this.set(false);
    }

    boolean get() {
        return this.output;
    }

}
