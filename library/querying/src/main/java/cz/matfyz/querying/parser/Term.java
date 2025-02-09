package cz.matfyz.querying.parser;

import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.querying.exception.ParsingException;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents either a variable (?variable), a literal ("literal") or an expression (SUM(?variable)).
 * Each term corresponds to an objex.
 */
public class Term implements ParserNode, Comparable<Term> {

    @Override public Term asTerm() {
        return this;
    }

    public Term(Constant constant) {
        this.constant = constant;
    }

    private @Nullable Constant constant;

    public boolean isConstant() {
        return constant != null;
    }

    public Constant asConstant() {
        if (constant != null)
            return constant;

        throw ParsingException.wrongNode(Constant.class, this);
    }

    public Term(Variable variable) {
        this.variable = variable;
    }

    private @Nullable Variable variable;

    public boolean isVariable() {
        return variable != null;
    }

    public Variable asVariable() {
        if (variable != null)
            return variable;

        throw ParsingException.wrongNode(Variable.class, this);
    }

    public Term(Computation computation) {
        this.computation = computation;
    }

    private @Nullable Computation computation;

    public boolean isComputation() {
        return computation != null;
    }

    public Computation asComputation() {
        if (computation != null)
            return computation;

        throw ParsingException.wrongNode(Computation.class, this);
    }

    // Common expression

    public Expression asExpression() {
        if (isConstant())
            return constant;

        if (isVariable())
            return variable;

        return computation;
    }

    // TODO Is it really necessary for the term to be comparable? Does it even make sense to compare expressions?

    private String getIdentifier() {
        if (constant != null)
            return "s_" + constant;

        if (variable != null)
            return "v_" + variable.name();

        return "a_" + computation.toString();
    }

    public boolean equals(Term other) {
        return getIdentifier().equals(other.getIdentifier());
    }

    @Override public int compareTo(Term other) {
        return getIdentifier().compareTo(other.getIdentifier());
    }

}
