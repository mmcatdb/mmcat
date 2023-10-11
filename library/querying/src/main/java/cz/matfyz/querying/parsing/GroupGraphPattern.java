package cz.matfyz.querying.parsing;

import java.util.List;

public class GroupGraphPattern implements ParserNode {

    public final List<WhereTriple> triples;
    public final List<ConditionFilter> conditionFilters;
    public final List<ValueFilter> valueFilters;

    GroupGraphPattern(List<WhereTriple> triples, List<ConditionFilter> conditionFilters, List<ValueFilter> valueFilters) {
        this.triples = triples;
        this.conditionFilters = conditionFilters;
        this.valueFilters = valueFilters;
    }

}