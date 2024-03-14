package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.ParserNode.Term;

public class Variable implements Term {

    @Override public Variable asVariable() {
        return this;
    }

    public final String name;
    private final boolean isOriginal;

    Variable(String name, boolean isOriginal) {
        this.name = name;
        this.isOriginal = isOriginal;
    }

    @Override public String getIdentifier() {
        return "v_" + name;
    }

    @Override public boolean isOriginal() {
        return isOriginal;
    }

    @Override public boolean equals(Object other) {
        return other instanceof Variable variable && variable.name.equals(name);
    }

    @Override public String toString() {
        return "?" + name;
    }

}
