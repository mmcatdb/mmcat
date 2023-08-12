package cz.matfyz.core.exception;

import cz.matfyz.core.instance.SuperIdWithValues;

import java.io.Serializable;
import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class ObjectException extends CoreException {
    
    private ObjectException(String name, Serializable data) {
        super("object." + name, data, null);
    }

    private record RowData(
        SuperIdWithValues superId,
        Set<String> technicalIds
    ) implements Serializable {}

    public static ObjectException actualRowNotFound(SuperIdWithValues superId, Set<String> technicalIds) {
        return new ObjectException("actualRowNotFound", new RowData(superId, technicalIds));
    }

}
