package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.cuni.matfyz.abstractwrappers.exception.QueryException;
import cz.cuni.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.cuni.matfyz.core.mapping.KindInstance;
import cz.cuni.matfyz.core.mapping.StaticName;

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
        var select = buildSelect();
        var query = select.stringContent();
        query += " " + buildFrom();
        query += " " + buildWhere();

        return new QueryStatement(query, select.nameMap());
    }

    private QueryStatement buildSelect() {
        var variableSelections = projections.values().stream().map(this::getVariableAlias).toList();
        var variableNameMap = new TreeMap<VariableIdentifier, List<String>>();
        projections.values().forEach(p -> variableNameMap.put(p.variableId, List.of(getVariableName(p))));
            
        var select = "SELECT " + String.join(", ", variableSelections);
        return new QueryStatement(select, variableNameMap);
    }

    private String getVariableSelection(Projection projection) {
        var staticName = (StaticName) projection.propertyPath.get(projection.propertyPath.size() - 1).name();
        return kinds.get(projection.kind) + "." + staticName.getStringName();
    }

    private String getVariableAlias(Projection projection) {
        var selection = getVariableSelection(projection);
        var alias = getVariableName(projection);
        return selection + " AS " + alias;
    }

    private String getVariableName(Projection projection) {
        var staticName = (StaticName) projection.propertyPath.get(projection.propertyPath.size() - 1).name();
        return kinds.get(projection.kind) + "_" + staticName.getStringName();
    }

    private String buildFrom() {
        if (joins.isEmpty()) {
            // TODO - I am not sure what the next-iter construction should mean.
            // if not _joins:
            //     table = next(iter(_kinds.values()), None)
            String table = kinds.values().iterator().next();
            if (table == null)
                throw QueryException.message("No tables are selected in FROM clause.");

            return "FROM " + table;
        }
        return "FROM " + buildJoins();
    }

    private String buildJoins() {
        var joinedKinds = new TreeSet<KindInstance>();
        var joinedTables = "";
        
        for (var join : joins) {
            KindInstance newKind;
            if (joinedKinds.contains(join.rhsKind))
                newKind = join.lhsKind;
            else {
                if (joinedKinds.isEmpty()) {
                    joinedKinds.add(join.lhsKind);
                    joinedTables += kinds.get(join.lhsKind);
                }

                newKind = join.rhsKind;
            }

            joinedKinds.add(newKind);
            var joinConditions = "";

            for (var property : join.joinProperties) {
                var lhsStaticName = (StaticName) property.lhsList().get(property.lhsList().size() - 1).name();
                var lhsProjection = kinds.get(join.lhsKind) + "." + lhsStaticName.getStringName();
                var rhsStaticName = (StaticName) property.rhsList().get(property.rhsList().size() - 1).name();
                var rhsProjection = kinds.get(join.rhsKind) + "." + rhsStaticName.getStringName();

                if (!joinConditions.equals(""))
                    joinConditions += " AND ";

                joinConditions += lhsProjection + " = " + rhsProjection;
            }

            joinedTables += " JOIN " + kinds.get(newKind) + " ON (" + joinConditions + ")";
        }
            
        return joinedTables;
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
            
            newValuesFilters.add(variableAlias + " IN (" + String.join(",", sqlValueStrings) + ")");
        }

        return String.join(" AND ", newValuesFilters);
    }

}