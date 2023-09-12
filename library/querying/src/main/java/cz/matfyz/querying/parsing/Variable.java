package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;

import java.util.Map;
import java.util.TreeMap;

public class Variable extends ParserNode implements ValueNode {

    @Override Variable asVariable() {
        return this;
    }

    @Override ValueNode asValueNode() {
        return this;
    }

    @Override public String name() {
        return name;
    }

    public final String name;
    public final VariableIdentifier id;
    
    private Variable(String name, VariableIdentifier id) {
        this.name = name;
        this.id = new VariableIdentifier(name);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Variable variable && variable.name.equals(name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static class VariableBuilder {

        private int lastIdentifier = 0;
        private Map<String, VariableIdentifier> nameToIdentifier = new TreeMap<>();
        
        private VariableIdentifier generateIdentifier() {
            return new VariableIdentifier("" + lastIdentifier++);
        }

        public Variable fromName(String name) {
            final var identifier = nameToIdentifier.computeIfAbsent(name, x -> generateIdentifier());
            return new Variable(name, identifier);
        }

        private int lastGeneratedNameId = 0;

        private String generateName() {
            return "#var" + lastGeneratedNameId++;
        }

        public Variable generated() {
            return new Variable(generateName(), generateIdentifier());
        }

    }

}