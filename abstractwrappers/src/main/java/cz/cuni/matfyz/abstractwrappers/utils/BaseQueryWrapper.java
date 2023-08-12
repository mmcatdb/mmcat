package cz.cuni.matfyz.abstractwrappers.utils;

import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.ConstantFilter;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.Join;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.JoinedProperty;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.Projection;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.ValuesFilter;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;
import cz.cuni.matfyz.abstractwrappers.AbstractQueryWrapper.VariablesFilter;
import cz.cuni.matfyz.abstractwrappers.exception.QueryException;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.KindInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseQueryWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseQueryWrapper.class);

    protected Map<KindInstance, String> kinds = new TreeMap<>();
    protected Map<VariableIdentifier, Projection> projections = new TreeMap<>();
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

    // public void defineKind(KindInstance kind, String kindName) {
    //     // LOGGER.info("[define kind] {}", kindName);
    //     kinds.put(kind, kindName);
    // }

    public void addProjection(List<AccessPath> propertyPath, KindInstance kind, VariableIdentifier variableId) {
        // LOGGER.info("[add projection]\n{}\n{}\n{}", propertyPath, kind, variableId);
        projections.put(variableId, new Projection(propertyPath, kind, variableId));
    }

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

    // public void addJoin(KindInstance lhsKind, List<JoinedProperty> joinProperties, KindInstance rhsKind) {
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
    
}
