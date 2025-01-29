package cz.matfyz.querying.parser;

import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.Operator;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.exception.ParsingException;

import java.util.List;
import java.util.stream.Collectors;

// TODO This might be unified with Expression somehow?
// However, we have to make sure the expression keeps all original information. Because both "?id = 1" and "1 = ?id" will be translated to (=, ?id, 1).

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
        Operator operator,
        Term rhs
    ) implements Filter {

        @Override public ConditionFilter asConditionFilter() {
            return this;
        }

        @Override public String toString() {
            return lhs + " " + operator + " " + rhs;
        }

    }

    public record ValueFilter(
        Variable variable,
        List<Constant> allowedValues
    ) implements Filter {

        @Override public ValueFilter asValueFilter() {
            return this;
        }

        @Override public String toString() {
            return variable + " IN (`" + allowedValues.stream().map(constant -> constant.value()).collect(Collectors.joining("`, `")) + "`)";
        }

    }

}
