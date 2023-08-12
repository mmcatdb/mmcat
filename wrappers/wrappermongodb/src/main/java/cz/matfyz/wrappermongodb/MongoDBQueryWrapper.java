package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.StaticName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MongoDBQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return false; }
    @Override public boolean isOptionalJoinSupported() { return true; }
    @Override public boolean isNonIdFilterSupported() { return true; }
    // CHECKSTYLE:ON

    @Override
    protected Map<ComparisonOperator, String> defineComparisonOperators() {
        var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.EQUALS, "$eq");
        output.put(ComparisonOperator.NOT_EQUALS, "$ne");
        output.put(ComparisonOperator.LESS_THAN, "$lt");
        output.put(ComparisonOperator.LESS_THAN_EQUALS, "$lte");
        output.put(ComparisonOperator.GREATER_THAN, "$gt");
        output.put(ComparisonOperator.GREATER_THAN_EQUALS, "$gte");
        return output;
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static String jsonDumps(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw QueryException.message("JsonProcessingException");
        }
    }

    public QueryStatement buildStatement() {
        var query = "db.";
        query += baseCollectionName();
        
        var aggregationPipeline = buildAggregationPipeline();
        query += ".aggregate(" + jsonDumps(aggregationPipeline) + ")";
        var variableNameMap = buildVariableNameMap();

        return new QueryStatement(query, variableNameMap);
    }

    private String baseCollectionName() {
        // final var kindNames = new TreeSet<>(projections.values().stream().map(projection -> kinds.get(projection.kind)).toList());
        final var kindNames = new TreeSet<>(projections.values().stream().map(projection -> projection.kind.mapping.kindName()).toList());
        if (kindNames.isEmpty())
            throw QueryException.message("No collection to aggregate on.");
        if (kindNames.size() > 1)
            throw QueryException.message("Joining collections in MongoDB is not supported since MM-evocat does not support it!");
        
        return kindNames.first();
    }

    private List<Object> buildAggregationPipeline() {
        var pipeline = new ArrayList<>();
        
        pipeline.add(buildConstFilters());
        pipeline.addAll(buildVariableFilters());
        pipeline.add(buildProjection());
        
        return pipeline;
    }

    private Map<String, Map<String, Integer>> buildProjection() {
        var projectionNames = new ArrayList<String>();
        for (var projection : projections.values())
            projectionNames.add(getVariableName(projection));
            
        // We support nested complex properties even for arrays, but MM-evocat does not support them yet, so their querying will not work.
        var project = new TreeMap<String, Integer>();
        for (var name : projectionNames)
            project.put(name, 1);
        
        return Map.of("$project", project);
    }

    private Map<String, Object> buildConstFilters() {
        var filters = new TreeMap<String, Object>();

        for (var constantFilter : constantFilters) {
            var projection = projections.get(constantFilter.variableId);
            var propName = (StaticName) projection.propertyPath.get(projection.propertyPath.size() - 1).name();

            filters.put(propName.getStringName(), Map.of(getOperatorValue(constantFilter.operator), constantFilter.constant));
        }

        for (var values : valuesFilters) {
            var projection = projections.get(values.variableId);
            var propName = (StaticName) projection.propertyPath.get(projection.propertyPath.size() - 1).name();
            if (values.constants.isEmpty())
                throw QueryException.message("VALUES clause for variable " + propName.getStringName() + " has no allowed values.");

            filters.put(propName.getStringName(), Map.of("$in", values.constants));
        }

        return Map.of("$match", filters);
    }

    private List<Map<String, Object>> buildVariableFilters() {
        var filterStages = new ArrayList<Map<String, Object>>();

        for (var filter : variablesFilters) {
            var lhsProjection = projections.get(filter.lhsVariableId);
            var lhsPropName = getVariableName(lhsProjection);

            var rhsProjection = projections.get(filter.rhsVariableId);
            var rhsPropName = getVariableName(rhsProjection);

            filterStages.add(
                Map.of("$match",
                    Map.of("$expr",
                        Map.of(getOperatorValue(filter.operator), List.of(lhsPropName, rhsPropName))
                    )
                )
            );
        }

        return filterStages;
    }

    private Map<VariableIdentifier, List<String>> buildVariableNameMap() {
        var output = new TreeMap<VariableIdentifier, List<String>>();
        projections.values().forEach(p -> output.put(p.variableId, getVariableNamePath(p)));

        return output;
    }

    private String getVariableName(Projection projection) {
        return String.join(".", getVariableNamePath(projection));
    }

    private List<String> getVariableNamePath(Projection projection) {
        return projection.propertyPath.stream().map(path -> (StaticName) path.name()).map(StaticName::getStringName).toList();
    }

}