package cz.matfyz.querying.resolver.queryresult;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * It's like ResultStructure, but it's transforming, so some of its properties are not known yet.
 */
public class TformingResultStructure {

    public final String inputName;
    public final String outputName;
    public final Variable variable;
    public final List<TformingResultStructure> children = new ArrayList<>();
    /** This is supposed to be determined later during the transformation. */
    private boolean isArray = false;
    private @Nullable Signature signatureFromParent = null;

    public boolean isArray() {
        return isArray;
    }

    public TformingResultStructure(String inputName, String outputName, Variable variable) {
        this.inputName = inputName;
        this.outputName = outputName;
        this.variable = variable;
    }

    public void setPathInfo(boolean isArray, Signature signatureFromParent) {
        this.isArray = isArray;
        this.signatureFromParent = signatureFromParent;
    }

    public ResultStructure toResultStructure() {
        final var output = new ResultStructure(outputName, isArray, variable);
        children.forEach(child -> output.addChild(child.toResultStructure(), child.signatureFromParent));

        return output;
    }

}
