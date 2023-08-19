package cz.matfyz.abstractwrappers;

import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Kind;

import java.util.List;
import java.util.Map;

public interface AbstractQueryWrapper {

    /**
     * Defines whether non-optional (inner) joins are supported.
     */
    boolean isJoinSupported();

    /**
     * Defines whether optional (left outer) joins are supported.
     */
    boolean isOptionalJoinSupported();

    /**
     * Defines whether it is possible to filter on properties which are not in an object's ids.
     */
    boolean isNonIdFilterSupported();

    public static class VariableIdentifier implements Comparable<VariableIdentifier> {

        private String value;

        public VariableIdentifier(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(VariableIdentifier other) {
            return value.compareTo(other.value);
        }

    }

    static record QueryStatement(
        String stringContent,
        Map<VariableIdentifier, List<String>> nameMap
    ) {}

    /**
     * Build the native query using this wrapper, returning a tuple `(native_query, variable_name_map)` where `native_query` is the compiled native database query,
     * and `variable_name_map` maps variable identifiers to final name paths within the native query result.
     */
    QueryStatement buildStatement();

    void addProjection(List<AccessPath> propertyPath, Kind kind, VariableIdentifier variableId);

    void addConstantFilter(VariableIdentifier variableId, ComparisonOperator operator, String constant);

    void addVariablesFilter(VariableIdentifier lhsVariableId, ComparisonOperator operator, VariableIdentifier rhsVariableId);

    void addValuesFilter(VariableIdentifier variableId, List<String> constants);

    void addJoin(String lhsKind, List<JoinedProperty> joinProperties, String rhsKind);
    
    enum ComparisonOperator {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        LESS_THAN_EQUALS,
        GREATER_THAN,
        GREATER_THAN_EQUALS,
    }

    /**
     * Base class for all operations which can be stored by the wrapper.
     */
    interface Operation {

    }

    /**
     * Operation corresponding to projection of a property, as defined by graph patterns in MMQL.
     */
    public static class Projection implements Operation {
        public final List<AccessPath> propertyPath;
        public final Kind kind;
        public final VariableIdentifier variableId;
        
        public Projection(List<AccessPath> propertyPath, Kind kind, VariableIdentifier variableId) {
            this.propertyPath = propertyPath;
            this.kind = kind;
            this.variableId = variableId;
        }
    }

    /**
     * Operation corresponding to a MMQL `FILTER` statement of the form `FILTER(?var op constant).
     */
    public static class ConstantFilter implements Operation {
        public final VariableIdentifier variableId;
        public final ComparisonOperator operator;
        public final String constant;

        public ConstantFilter(VariableIdentifier variableId, ComparisonOperator operator, String constant) {
            this.variableId = variableId;
            this.operator = operator;
            this.constant = constant;
        }
    }

    /**
     * Operation corresponding to a MMQL `FILTER` statement of the form `FILTER(?var1 op ?var2)`.
     */
    public static class VariablesFilter implements Operation {
        public final VariableIdentifier lhsVariableId;
        public final ComparisonOperator operator;
        public final VariableIdentifier rhsVariableId;
        
        public VariablesFilter(VariableIdentifier lhsVariableId, ComparisonOperator operator, VariableIdentifier rhsVariableId) {
            this.lhsVariableId = lhsVariableId;
            this.operator = operator;
            this.rhsVariableId = rhsVariableId;
        }
    }

    /**
     * Operation corresponding to a MMQL `VALUES` statement of the form `VALUES ?var {constant1, constant2}`.
     */
    public static class ValuesFilter implements Operation {
        public final VariableIdentifier variableId;
        public final List<String> constants;

        public ValuesFilter(VariableIdentifier variableId, List<String> constants) {
            this.variableId = variableId;
            this.constants = constants;
        }
    }

    // type JoinProperties = List<Tuple<List<AccessPath>, List<AccessPath>>>
    public static record JoinedProperty(
        List<AccessPath> lhsList,
        List<AccessPath> rhsList
    ) {}

    /**
     * Operation corresponding to an inner join between the two specified kinds on the specified properties.
     * The `join_properties` contains a list of tuples, each of which contains a property path from the left kind, meaning that this property should be inner joined on equality to the corresponding property from the right kind.
     */
    public static class Join implements Operation {
        public final String lhsKind;
        public final List<JoinedProperty> joinProperties;
        public final String rhsKind;

        public Join(String lhsKind, List<JoinedProperty> joinProperties, String rhsKind) {
            this.lhsKind = lhsKind;
            this.joinProperties = joinProperties;
            this.rhsKind = rhsKind;
        }
    }
}
