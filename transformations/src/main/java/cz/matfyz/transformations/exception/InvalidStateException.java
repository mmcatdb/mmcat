package cz.matfyz.transformations.exception;

import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.Name;
import cz.matfyz.core.record.SimpleRecord;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class InvalidStateException extends TransformationException {

    private InvalidStateException(String name, Serializable data) {
        super("invalidState." + name, data, null);
    }

    public static InvalidStateException simpleRecordIsNotValue(SimpleRecord<?> simpleRecord) {
        return new InvalidStateException("simpleRecordIsNotValue", simpleRecord);
    }

    public static InvalidStateException complexRecordHasArrayValue() {
        return new InvalidStateException("complexRecordHasArrayValue", null);
    }

    public static InvalidStateException dynamicNameNotFound(DynamicName dynamicName) {
        return new InvalidStateException("dynamicNameNotFound", dynamicName);
    }

    public static InvalidStateException nameIsNotStatic(Name name) {
        return new InvalidStateException("nameIsNotStatic", name);
    }

}
