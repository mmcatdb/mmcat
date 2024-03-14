package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.ParserNode.Term;

/**
 * Each string value should be treated as unique, even if their content is the same.
 */
public class StringValue implements Term {

    @Override public StringValue asStringValue() {
        return this;
    }

    public final String value;
    private final String id;

    StringValue(String value, String id) {
        this.value = value;
        this.id = id;
    }

    @Override public String getIdentifier() {
        return "s_" + id;
    }

    @Override public boolean equals(Object other) {
        return other instanceof StringValue wrapper && wrapper.id.equals(id);
    }

    @Override public String toString() {
        return this.id + "(" + value + ")";
    }

}
