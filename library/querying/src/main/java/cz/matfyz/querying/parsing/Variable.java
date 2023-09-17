package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.Map;
import java.util.TreeMap;

public class Variable implements Term {

    @Override public Variable asVariable() {
        return this;
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
        return "?" + name;
    }

    static class VariableBuilder {

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