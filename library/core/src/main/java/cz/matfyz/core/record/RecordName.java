package cz.matfyz.core.record;

import java.io.Serializable;

/**
 * @author jachym.bartik
 */
public abstract class RecordName implements Serializable {

    protected final String value;

    public String value() {
        return value;
    }

    protected RecordName(String value) {
        this.value = value;
    }

}
