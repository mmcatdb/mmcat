package cz.matfyz.core.querying;

import java.util.Map;
import java.util.TreeMap;

public record Variable(
    String name,
    boolean isOriginal
) implements Comparable<Variable>, Expression {

    @Override public boolean equals(Object other) {
        return other instanceof Variable variable && variable.name.equals(name);
    }

    @Override public int hashCode() {
        return name.hashCode();
    }

    @Override public int compareTo(Variable other) {
        return name.compareTo(other.name);
    }

    @Override public String toString() {
        return "?" + name;
    }

    /**
     * A variable is uniquely identified by its name (inside a scope).
     * Nested clauses share the same scope! Only nested queries create a new scope (which is not supported yet).
     */
    public static class VariableScope {

        VariableScope() {}

        private Map<String, Variable> nameToVariable = new TreeMap<>();

        public Variable createOriginal(String name) {
            final var current = nameToVariable.get(name);
            if (current == null) {
                final var newVariable = new Variable(name, true);
                nameToVariable.put(name, newVariable);
                return newVariable;
            }

            if (!current.isOriginal)
                throw new IllegalStateException("Variable name is already used by a generated variable: " + name);

            return current;
        }

        public Variable createGenerated() {
            final var variable = new Variable(generateVariableName(), false);

            // We don't want to allow accidental mixing of original and generated variables.
            // Some of the original variables might be processed after the generated variables. So, we just eliminate any possibility of such situation.
            if (nameToVariable.containsKey(variable.name))
                throw new IllegalStateException("Generated variable name is already in use: " + variable.name);

            nameToVariable.put(variable.name, variable);

            return variable;
        }

        private int lastGeneratedNameId = 0;

        private String generateVariableName() {
            return "#var" + lastGeneratedNameId++;
        }

        // Scope nesting

        // Idea:
        //  - Nested scopes can shadow variables from outer scopes (just like in JS).
        //      - However, shadowing is a default behavior, unless explicitly disabled (for some specific variables).
        //      - These would be the variables that are "exported" from the inner scope by a "SELECT" clause.
        //  - However, we don't support nested queries yet.
        //  - A potential problem with variable resolution - if a variable is generated, its status should depend on its parent and children variables.

        /*
        private @Nullable VariableScope parent;

        private VariableScope(@Nullable VariableScope parent) {
            this.parent = parent;
        }

        public static VariableScope createRootScope() {
            return new VariableScope(null);
        }

        public VariableScope createNestedScope() {
            return new VariableScope(this);
        }

        public @Nullable Variable resolve(String name) {

        }
        */

    }

}
