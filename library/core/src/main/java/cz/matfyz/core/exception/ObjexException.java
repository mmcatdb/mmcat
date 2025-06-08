package cz.matfyz.core.exception;

import cz.matfyz.core.instance.SuperIdValues;

import java.io.Serializable;
import java.util.Set;

public class ObjexException extends CoreException {

    private ObjexException(String name, Serializable data) {
        super("object." + name, data, null);
    }

    private record RowData(
        SuperIdValues values,
        Set<String> technicalIds
    ) implements Serializable {}

    public static ObjexException actualRowNotFound(SuperIdValues values, Set<String> technicalIds) {
        return new ObjexException("actualRowNotFound", new RowData(values, technicalIds));
    }

}
