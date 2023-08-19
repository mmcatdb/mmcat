package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.StaticName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class PostgreSQLQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return true; }
    @Override public boolean isOptionalJoinSupported() { return true; }
    @Override public boolean isNonIdFilterSupported() { return true; }
    // CHECKSTYLE:ON

    @Override
    protected Map<ComparisonOperator, String> defineComparisonOperators() {
        var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.EQUALS, "=");
        output.put(ComparisonOperator.NOT_EQUALS, "<>");
        output.put(ComparisonOperator.LESS_THAN, "<");
        output.put(ComparisonOperator.LESS_THAN_EQUALS, "<=");
        output.put(ComparisonOperator.GREATER_THAN, ">");
        output.put(ComparisonOperator.GREATER_THAN_EQUALS, ">=");
        return output;
    }

    @Override
    public QueryStatement buildStatement() {
        final QueryStatement select = buildSelect();
        final String query = select.stringContent() + "\n" + buildFrom() + "\n" + buildWhere();

        return new QueryStatement(query, select.nameMap());
    }

    private QueryStatement buildSelect() {
        var variableSelections = projections.values().stream().map(this::getVariableAlias).toList();
        var variableNameMap = new TreeMap<VariableIdentifier, List<String>>();
        projections.values().forEach(p -> variableNameMap.put(p.variableId, List.of(getVariableName(p))));
            
        var select = "SELECT " + String.join(", ", variableSelections);
        return new QueryStatement(select, variableNameMap);
    }

    private String getVariableAlias(Projection projection) {
        return getVariableSelection(projection) + " AS " + getVariableName(projection);
    }

    private String getVariableSelection(Projection projection) {
        return escapeKindName(projection.kind.mapping.kindName()) + "." + getPropertyName(projection.propertyPath);
    }

    private String getVariableName(Projection projection) {
        // return kinds.get(projection.kind) + "_" + getPropertyName(projection.propertyPath);
        return projection.kind.mapping.kindName() + "_" + getPropertyName(projection.propertyPath);
    }

    private String escapeKindName(String name) {
        return "\"" + name + "\"";
    }

    private String getPropertyName(List<AccessPath> path) {
        if (path.get(path.size() - 1).name() instanceof StaticName staticName)
            return staticName.getStringName();

        throw QueryException.message("Property name is dynamic.");
    }

    private String buildFrom() {
        if (!joins.isEmpty())
            return "FROM " + buildJoins();
        
        // final String firstKind = kinds.values().stream().findFirst().orElseThrow(() -> QueryException.message("No tables are selected in FROM clause."));
        final String firstKind = projections.values().stream().findFirst()
            .orElseThrow(() -> QueryException.message("No tables are selected in FROM clause."))
            .kind.mapping.kindName();

        return "FROM " +  escapeKindName(firstKind);
    }

    private String buildJoins() {
        final var joinedKinds = new TreeSet<String>();

        final String fromKind = joins.get(0).lhsKind;
        joinedKinds.add(fromKind);

        var output = escapeKindName(fromKind);
        
        for (final var join : joins) {
            String newKind;
            if (!joinedKinds.contains(join.rhsKind))
                newKind = join.rhsKind;
            else if (!joinedKinds.contains(join.lhsKind))
                newKind = join.lhsKind;
            else
                continue;

            joinedKinds.add(newKind);

            final var joinConditions = join.joinProperties.stream().map(property -> {
                final String lhsProjection = escapeKindName(join.lhsKind) + "." + getPropertyName(property.lhsList());
                final String rhsProjection = escapeKindName(join.rhsKind) + "." + getPropertyName(property.rhsList());
                
                return lhsProjection + " = " + rhsProjection;
            }).toList();

            final String allConditions = String.join(" AND ", joinConditions);
            output += " JOIN " + escapeKindName(newKind) + " ON (" + allConditions + ")";
        }
            
        return output;
    }

    private String buildWhere() {
        var filters = buildFilters();
        var newValuesFilters = buildValues();
        
        var oneOrOther = filters.equals("") ? newValuesFilters : filters;
        if (oneOrOther.equals(""))
            return "";

        var whereConditions = (!filters.equals("") && !newValuesFilters.equals(""))
            ? filters + " AND " + newValuesFilters
            : oneOrOther;
        
        return "WHERE " + whereConditions;
    }

    private String buildFilters() {
        var filters = new ArrayList<String>();

        for (var filter : constantFilters) {
            // We can't use the variable name since in SQL, we cannot use an alias in the WHERE clause.
            var projection = projections.get(filter.variableId);
            var variableAlias = getVariableSelection(projection);

            filters.add(variableAlias + " " + getOperatorValue(filter.operator) + " '" + filter.constant + "'");
        }

        for (var filter : variablesFilters) {
            var lhsProjection = projections.get(filter.lhsVariableId);
            var lhsVariableAlias = getVariableSelection(lhsProjection);

            var rhsProjection = projections.get(filter.rhsVariableId);
            var rhsVariableAlias = getVariableSelection(rhsProjection);

            filters.add(lhsVariableAlias + " " + getOperatorValue(filter.operator) + " " + rhsVariableAlias);
        }

        return String.join(" AND ", filters);
    }

    private String buildValues() {
        var newValuesFilters = new ArrayList<String>();

        for (var values : valuesFilters) {
            var projection = projections.get(values.variableId);
            var variableAlias = getVariableSelection(projection);

            if (values.constants.isEmpty())
                throw QueryException.message("VALUES clause for variable " + variableAlias + " has no allowed values.");

            var sqlValueStrings = values.constants.stream().map(c -> "'" + c + "'").toList();
            
            newValuesFilters.add(variableAlias + " IN (" + String.join(", ", sqlValueStrings) + ")");
        }

        return String.join(" AND ", newValuesFilters);
    }

}