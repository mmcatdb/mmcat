package cz.matfyz.querying.parser;

import cz.matfyz.core.querying.Computation;

public record Filter(
    Computation computation
) implements ParserNode {

    @Override public Filter asFilter() {
        return this;
    }

    // default ConditionFilter asConditionFilter() {
    //     throw ParsingException.wrongNode(ConditionFilter.class, this);
    // }

    // default ValueFilter asValueFilter() {
    //     throw ParsingException.wrongNode(ValueFilter.class, this);
    // }

    // public record ConditionFilter(
    //     Term lhs,
    //     Operator operator,
    //     Term rhs
    // ) implements Filter {

    //     @Override public ConditionFilter asConditionFilter() {
    //         return this;
    //     }

    //     @Override public String toString() {
    //         return lhs + " " + operator + " " + rhs;
    //     }

    // }

    // public record ValueFilter(
    //     Variable variable,
    //     List<Constant> allowedValues
    // ) implements Filter {

    //     @Override public ValueFilter asValueFilter() {
    //         return this;
    //     }

    //     @Override public String toString() {
    //         return variable + " IN (`" + allowedValues.stream().map(constant -> constant.value()).collect(Collectors.joining("`, `")) + "`)";
    //     }

    // }

}
