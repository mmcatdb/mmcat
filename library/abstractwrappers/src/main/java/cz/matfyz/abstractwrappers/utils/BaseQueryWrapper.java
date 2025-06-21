package cz.matfyz.abstractwrappers.utils;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AbstractWrapperContext;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.ResultStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseQueryWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseQueryWrapper.class);

    // Projections

    /**
     * @param structure A leaf in the result structure tree. Used to determine where to put the projection in the final result.
     */
    protected record Projection(Property property, ResultStructure structure, boolean isOptional) {}

    protected List<Projection> projections = new ArrayList<>();

    // TODO there should be some check if the projection isn't already defined. Probably by its variable? Or by the corresponding objex?
    public void addProjection(Property property, ResultStructure structure, boolean isOptional) {
        projections.add(new Projection(property, structure, isOptional));
    }

    // Joins

    protected record Join(Mapping from, Mapping to, Signature fromPath, Signature toPath, int repetition, boolean isOptional) {}

    protected List<Join> joins = new ArrayList<>();

    public void addJoin(Mapping from, Mapping to, Signature fromPath, Signature toPath, int repetition, boolean isOptional) {
        joins.add(new Join(from, to, fromPath, toPath, repetition, isOptional));
    }

    // Filters

    protected interface Filter {}

    protected record UnaryFilter(Property property, Constant constant, Operator operator) implements Filter {}
    protected record BinaryFilter(Property property1, Property property2, Operator operator) implements Filter {}
    protected record SetFilter(Property property, List<Constant> set, Operator operator) implements Filter {}

    protected List<Filter> filters = new ArrayList<>();

    public void addFilter(Property property, Constant constant, Operator operator) {
        filters.add(new UnaryFilter(property, constant, operator));
    }

    public void addFilter(Property property1, Property property2, Operator operator) {
        filters.add(new BinaryFilter(property1, property2, operator));
    }

    public void addFilter(Property property, List<Constant> set, Operator operator) {
        filters.add(new SetFilter(property, set, operator));
    }

    // Context

    protected AbstractWrapperContext context;

    public void setContext(AbstractWrapperContext context) {
        this.context = context;
    }

    /**
     * Maps {@link Operator} to its string representation in the target DSL.
     */
    public static class Operators {

        private final Map<Operator, String> operatorToString = new TreeMap<Operator, String>();
        private final Map<String, Operator> stringToOperator = new TreeMap<String, Operator>();

        public void define(Operator operator, String value) {
            operatorToString.put(operator, value);
            stringToOperator.put(value, operator);
        }

        public String stringify(Operator operator) {
            final var value = operatorToString.get(operator);
            if (value == null)
                throw QueryException.unsupportedOperator(operator);

            return value;
        }

        public Operator parse(String value) {
            final var operator = stringToOperator.get(value);
            if (operator == null)
                throw QueryException.unsupportedOperator(value);

            return operator;
        }

    }

}
