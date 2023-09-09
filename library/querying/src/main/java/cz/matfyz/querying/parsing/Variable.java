package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;

import java.util.UUID;

public class Variable extends QueryNode implements ValueNode {

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
    
    private Variable(String name) {
        this.name = name;
        this.id = new VariableIdentifier(name);
    }

    public static Variable fromName(String name) {
        return new Variable(name);
    }

    public static Variable generated() {
        return new Variable(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Variable variable && variable.name.equals(name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}