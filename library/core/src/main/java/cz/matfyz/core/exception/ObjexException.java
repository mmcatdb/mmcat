package cz.matfyz.core.exception;

import cz.matfyz.core.instance.SuperIdValues;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ObjexException extends CoreException {

    private ObjexException(String name, Serializable data) {
        super("object." + name, data, null);
    }

    private record RowData(
        SuperIdValues values,
        @Nullable Integer technicalId
    ) implements Serializable {}

    public static ObjexException actualRowNotFound(SuperIdValues values, Integer technicalId) {
        return new ObjexException("actualRowNotFound", new RowData(values, technicalId));
    }

}
