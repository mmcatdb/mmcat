package cz.matfyz.querying.parsing;

public class StringValue extends ParserNode implements ValueNode {

    @Override StringValue asStringValue() {
        return this;
    }

    @Override ValueNode asValueNode() {
        return this;
    }

    @Override public String name() {
        return value;
    }

    public final String value;

    public StringValue(String value) {
        this.value = value;
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