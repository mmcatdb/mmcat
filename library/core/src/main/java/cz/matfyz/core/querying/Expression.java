package cz.matfyz.core.querying;

import cz.matfyz.core.querying.Computation.ComputationScope;
import cz.matfyz.core.querying.Variable.VariableScope;

import java.io.Serializable;

/**
 * Something that can be evaluated to a value.
 * Variable, Constant, or Computation.
 */
public interface Expression extends Serializable {

    public record Constant(
        String value
    ) implements Expression {

        @Override public String toString() {
            return value;
        }

    }

    /**
     * A utility class for managing scopes of variables, computations, etc.
     */
    public static class ExpressionScope {

        public final VariableScope variable = new VariableScope();

        public final ComputationScope computation = new ComputationScope();

    }

}
