package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.ParserNode.Term;

public class StringValue implements Term {

    @Override public StringValue asStringValue() {
        return this;
    }

    public final String value;

    StringValue(String value) {
        this.value = value;
    }

    @Override
    public String getIdentifier() {
        return "s_" + value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StringValue wrapper && wrapper.value.equals(value);
    }

    @Override
    public String toString() {
        return this.value;
    }

}