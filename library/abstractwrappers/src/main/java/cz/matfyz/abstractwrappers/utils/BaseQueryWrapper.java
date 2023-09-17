package cz.matfyz.abstractwrappers.utils;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Constant;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.mapping.AccessPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseQueryWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseQueryWrapper.class);

    protected Map<Kind, String> kinds = new TreeMap<>();

    protected record Projection(Property property, boolean isOptional) {}

    protected List<Projection> projections = new ArrayList<>();
    protected List<ConstantFilter> constantFilters = new ArrayList<>();
    protected List<VariablesFilter> variablesFilters = new ArrayList<>();
    protected List<ValuesFilter> valuesFilters = new ArrayList<>();
    protected List<Join> joins = new ArrayList<>();

    protected abstract Map<ComparisonOperator, String> defineComparisonOperators();

    private final Map<ComparisonOperator, String> operators = defineComparisonOperators();

    protected String getOperatorValue(ComparisonOperator operator) {
        var value = operators.get(operator);
        if (value == null)
            throw QueryException.unsupportedOperator(operator);
            
        return value;
    }

    // TODO OLD BENEATH

    public void addConstantFilter(VariableIdentifier variableId, ComparisonOperator operator, String constant) {
        // LOGGER.info("[add constant filter]\n{}\n{}\n{}", variableId, operator, constant);
        constantFilters.add(new ConstantFilter(variableId, operator, constant));
    }

    public void addVariablesFilter(VariableIdentifier lhsVariableId, ComparisonOperator operator, VariableIdentifier rhsVariableId) {
        // LOGGER.info("[add variables filter]\n{}\n{}\n{}", lhsVariableId, operator, rhsVariableId);
        variablesFilters.add(new VariablesFilter(lhsVariableId, operator, rhsVariableId));
    }

    public void addValuesFilter(VariableIdentifier variableId, List<String> constants) {
        // LOGGER.info("[add values filter]\n{}\n{}", variableId, constants);
        valuesFilters.add(new ValuesFilter(variableId, constants));
    }

    public void addJoin(String lhsKind, List<JoinedProperty> joinProperties, String rhsKind) {
        LOGGER.info("[add join]\n{}\n{}\n{}", lhsKind, joinProperties, rhsKind);
        for (final var join : joins) {
            if (lhsKind.equals(join.lhsKind) && rhsKind.equals(join.rhsKind)) {
                LOGGER.info("Duplicate join found.");
                return;
            }

            if (lhsKind.equals(join.rhsKind) && rhsKind.equals(join.lhsKind)) {
                LOGGER.info("Duplicate reverse join found.");
                return;
            }
        }

        joins.add(new Join(lhsKind, joinProperties, rhsKind));
    }

    // TODO OLD ABOVE
    
    public void addProjection(Property property, boolean isOptional) {
        projections.add(new Projection(property, isOptional));
    }

    public void addJoin(Property from, Property to, int repetition, boolean isOptional) {
        
    }

    public void addFilter(FilterProperty left, FilterProperty right, ComparisonOperator operator) {
        
    }

    public void addFilter(FilterProperty left, Constant right, ComparisonOperator operator) {
        
    }
    
}
