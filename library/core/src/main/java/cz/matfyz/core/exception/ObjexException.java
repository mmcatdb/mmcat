package cz.matfyz.core.exception;

import cz.matfyz.core.instance.SuperIdWithValues;

import java.io.Serializable;
import java.util.Set;

public class ObjexException extends CoreException {

    private ObjexException(String name, Serializable data) {
        super("object." + name, data, null);
    }

    private record RowData(
        SuperIdWithValues superId,
        Set<String> technicalIds
    ) implements Serializable {}

    public static ObjexException actualRowNotFound(SuperIdWithValues superId, Set<String> technicalIds) {
        return new ObjexException("actualRowNotFound", new RowData(superId, technicalIds));
    }

}
