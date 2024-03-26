package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AggregationOperator;
import cz.matfyz.querying.exception.ParsingException;

/**
 * This interface represents either a variable (?variable), a literal ("literal") or an aggregation (SUM(?variable)).
 * Each term corresponds to a schema object. Therefore, they have to be distinguishable from each other terms so that we can map them unambiguously to the objects.
 */
public interface Term extends ParserNode, Comparable<Term> {

    String getIdentifier();

    /** If the term was included in the original query. If not, it was generated during the splitting of morphisms. */
    default boolean isOriginal() {
        return true;
    }

    default boolean equals(Term other) {
        return getIdentifier().equals(other.getIdentifier());
    }

    @Override default int compareTo(Term other) {
        return getIdentifier().compareTo(other.getIdentifier());
    }

    @Override default Term asTerm() {
        return this;
    }

    default StringValue asStringValue() {
        throw ParsingException.wrongNode(StringValue.class, this);
    }

    default Variable asVariable() {
        throw ParsingException.wrongNode(Variable.class, this);
    }

    default Aggregation asAggregation() {
        throw ParsingException.wrongNode(Aggregation.class, this);
    }

    /**
     * Each string value should be treated as unique, even if their content is the same.
     */
    public record StringValue(
        String value,
        String id
    ) implements Term {

        @Override public StringValue asStringValue() {
            return this;
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

    public record Variable(
        String name,
        boolean isOriginal
    ) implements Term {

        @Override public Variable asVariable() {
            return this;
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

    public record Aggregation(
        AggregationOperator operator,
        Variable variable,
        boolean isDistinct
    ) implements Term {

        @Override public Aggregation asAggregation() {
            return this;
        }

        @Override public String getIdentifier() {
            return "a_" + operator + "_" + variable.name;
        }
    
        @Override public boolean equals(Object other) {
            return other instanceof Aggregation aggregation && variable.equals(aggregation.variable) && operator == aggregation.operator;
        }
    
        @Override public String toString() {
            return operator.toString() + "(" + variable.toString() + ")";
        }

    }

    public static class Builder {

        // Variables

        public Variable variable(String name) {
            return new Variable(name, true);
        }

        public Variable generatedVariable() {
            return new Variable(generateVariableName(), false);
        }

        private int lastVariableNameId = 0;

        private String generateVariableName() {
            return "#var" + lastVariableNameId++;
        }

        // String values

        public StringValue stringValue(String value) {
            return new StringValue(value, generateStringValueId());
        }

        private int lastStringValueId = 0;

        private String generateStringValueId() {
            return "#str_" + lastStringValueId++;
        }

    }

}