package cz.matfyz.querying.algorithms.queryresult;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * It's like QueryStructure, but it's transforming so some of its properties are not known yet.
 */
public class TformingQueryStructure {

    public final String inputName;
    public final String outputName;
    public final SchemaObject schemaObject;
    public final List<TformingQueryStructure> children = new ArrayList<>();
    /** This is supposed to be determined later during the transformation. */
    private boolean isArray = false;
    @Nullable
    private Signature signatureFromParent = null;

    public boolean isArray() {
        return isArray;
    }

    public TformingQueryStructure(String inputName, String outputName, SchemaObject schemaObject) {
        this.inputName = inputName;
        this.outputName = outputName;
        this.schemaObject = schemaObject;
    }

    public void setPathInfo(boolean isArray, Signature signatureFromParent) {
        this.isArray = isArray;
        this.signatureFromParent = signatureFromParent;
    }

    public QueryStructure toQueryStructure() {
        final var output = new QueryStructure(outputName, isArray, schemaObject);
        children.forEach(child -> output.addChild(child.toQueryStructure(), signatureFromParent));

        return output;
    }

}
