package cz.matfyz.querying.parsing;

import cz.matfyz.querying.exception.ParsingException;

import java.io.Serializable;

public interface ParserNode extends Serializable {

    default Filter asFilter() {
        throw ParsingException.wrongNode(Filter.class, this);
    }

    default Term asTerm() {
        throw ParsingException.wrongNode(Term.class, this);
    }

    public interface Filter extends ParserNode {

        @Override default Filter asFilter() {
            return this;
        }

        default ConditionFilter asConditionFilter() {
            throw ParsingException.wrongNode(ConditionFilter.class, this);
        }

        default ValueFilter asValueFilter() {
            throw ParsingException.wrongNode(ValueFilter.class, this);
        }

    }

    /**
     * This interface represents either a variable (?variable), a literal ("literal") or an aggregation (SUM(?variable)).
     */
    public interface Term extends ParserNode, Comparable<Term> {

        @Override default Term asTerm() {
            return this;
        }

        String getIdentifier();

        /** If the term was included in the original query, or if it was generated during splitting of morphisms. */
        default boolean isOriginal() {
            return true;
        }

        default boolean equals(Term other) {
            return getIdentifier().equals(other.getIdentifier());
        }

        @Override default int compareTo(Term other) {
            return getIdentifier().compareTo(other.getIdentifier());
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

    }

}
