package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.querying.exception.ParsingException;
import cz.matfyz.querying.parsing.Term.Variable;

import java.util.List;

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

    public record ConditionFilter(
        Term lhs,
        ComparisonOperator operator,
        Term rhs
    ) implements Filter {

        @Override public ConditionFilter asConditionFilter() {
            return this;
        }

    }

    public record ValueFilter(
        // TODO why this can't be an aggregation?
        Variable variable,
        List<String> allowedValues

    ) implements Filter {

        @Override public ValueFilter asValueFilter() {
            return this;
        }

    }

}
