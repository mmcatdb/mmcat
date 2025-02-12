package cz.matfyz.core.querying;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.DoubleStream;

public class Computation implements Expression, Comparable<Computation> {

    public final Operator operator;
    public final List<Expression> arguments;

    /** Unique string representation of the function. It contains all its arguments (also stringified), so it might be quite long. */
    private final String stringValue;
    /** Used for comparing with other function expressions. Basically a much shorter version of stringValue. Needs to be set by the factory. */
    private String identifier;

    private Computation(Operator operator, List<Expression> arguments) {
        this.operator = operator;
        this.arguments = arguments;
        // We precompute the unique value because it will be used several times in comparisons etc.
        this.stringValue = computeStringValue();
    }

    private String computeStringValue() {
        final var builder = new StringBuilder();
        builder.append("[").append(operator).append("](");

        // We want to sort the arguments (if that's possitble) to make sure expressions like "?a = ?b" and "?b = ?a" are considered the same.
        final var sortedArguments = getSortedArguments();

        builder.append(sortedArguments.get(0));
        for (int i = 1; i < sortedArguments.size(); i++)
            builder.append(", ").append(sortedArguments.get(i));

        builder.append(")");

        return builder.toString();
    }

    private List<String> getSortedArguments() {
        if (arguments.size() == 1)
            return List.of(arguments.get(0).toString());

        var output = arguments.stream().map(Object::toString);
        if (isSortable())
            output = output.sorted();

        return output.toList();
    }

    private boolean isSortable() {
        if (operator.isSet())
            // Sets are always sortable.
            return true;
        if (operator.isAggregation())
            // Aggregations have two arguments (property and root), the second one is optional. The order is obviously important.
            return false;
        if (operator.isComparison())
            // This might need some adjustment as we add more operators.
            return operator == Operator.Equal || operator == Operator.NotEqual;

        throw new RuntimeException("Unknown operator type: " + operator);
    }

    public String identifier() {
        return identifier;
    }

    @Override public String toString() {
        return stringValue;
    }

    @Override public boolean equals(Object other) {
        return other instanceof Computation otherFunction && identifier.equals(otherFunction.identifier);
    }

    @Override public int compareTo(Computation other) {
        return identifier.compareTo(other.identifier);
    }

    /**
     * An expression is uniquely identified by its string value (inside a scope).
     * However, for easier comparison, we also create a shorter identifier.
     */
    public static class ComputationScope {

        ComputationScope() {}

        private Map<String, Computation> stringValueToComputation = new TreeMap<>();

        public Computation create(Operator operator, List<Expression> arguments) {
            final var computation = new Computation(operator, arguments);
            final var existing = stringValueToComputation.get(computation.stringValue);
            if (existing != null)
                return existing;

            // The computations needs to be unique by their string values, but we also generate a shorter identifier for easier comparison.
            computation.identifier = generateComputationName();
            stringValueToComputation.put(computation.stringValue, computation);

            return computation;
        }

        private int lastGeneratedNameId = 0;

        private String generateComputationName() {
            return "#fun" + lastGeneratedNameId++;
        }

        public Computation create(Operator operator, Expression... arguments) {
            return create(operator, List.of(arguments));
        }

    }

    // No, separate enums won't work here because even though they can extend interfaces, they can't reimplement the Comparable<Operator> interface. And that's the one we needed for any kind of set or map.
    // And yes, this looks horrendous. But it's the only way to make it work. Thanks, java.

    public enum OP {
        Comparison,
        Aggregation,
        Set,
        // TODO This is not yet implemented in the grammar (or like, anywhere).
        String;
    }

    public enum Operator {

        Equal           (OP.Comparison),
        NotEqual        (OP.Comparison),
        Less            (OP.Comparison),
        LessOrEqual     (OP.Comparison),
        Greater         (OP.Comparison),
        GreaterOrEqual  (OP.Comparison),

        Count           (OP.Aggregation),
        CountDistinct   (OP.Aggregation),
        Sum             (OP.Aggregation),
        Min             (OP.Aggregation),
        Max             (OP.Aggregation),
        Average         (OP.Aggregation),

        In              (OP.Set),
        NotIn           (OP.Set),

        Concatenate     (OP.String);

        private OP type;

        private Operator(OP type) {
            this.type = type;
        }

        /**
         * Expected inputs:
         *  - ?a <operator> ?b
         *  - ?a <operator> "value"
         */
        public boolean isComparison() {
            return type == OP.Comparison;
        }

        public boolean isAggregation() {
            return type == OP.Aggregation;
        }

        /**
         * Expected inputs:
         *  - ?a <operator> "value1" "value2" ...
         */
        public boolean isSet() {
            return type == OP.Set;
        }

        public boolean isString() {
            return type == OP.String;
        }

    }

    /**
     * The values might have a different semantics than the arguments of the computation!
     * E.g., an aggregation might have two arguments, a property and a reference node. However, in that case, this function would expect only a list of the actual values.
     */
    public String resolve(List<String> values) {
        if (operator.isComparison())
            return resolveComparison(values) ? "true" : "false";

        if (operator.isAggregation())
            return String.valueOf(resolveAggregation(values));

        return resolveSet(values) ? "true" : "false";
    }

    private boolean resolveComparison(List<String> values) {
        final String a = values.get(0);
        final String b = values.get(1);

        if (operator == Operator.Equal)
            return a.equals(b);
        if (operator == Operator.NotEqual)
            return !a.equals(b);

        final double x = Double.parseDouble(a);
        final double y = Double.parseDouble(b);

        return switch (operator) {
            case Operator.Less -> x < y;
            // TODO Maybe we should add some epsilon here?
            case Operator.LessOrEqual -> x <= y;
            case Operator.Greater -> x > y;
            case Operator.GreaterOrEqual -> x >= y;
            default -> throw new RuntimeException("Unknown operator: " + operator);
        };
    }

    private double resolveAggregation(List<String> values) {
        if (operator == Operator.Count)
            return values.size();

        final DoubleStream numbers = values.stream().mapToDouble(Double::parseDouble);

        return switch (operator) {
            case Operator.CountDistinct -> numbers.distinct().count();
            case Operator.Sum -> numbers.sum();
            case Operator.Min -> numbers.min().orElse(Double.NaN);
            case Operator.Max -> numbers.max().orElse(Double.NaN);
            case Operator.Average -> numbers.average().orElse(Double.NaN);
            default -> throw new RuntimeException("Unknown operator: " + operator);
        };
    }

    private boolean resolveSet(List<String> values) {
        final var value = values.get(0);

        for (int i = 1; i < values.size(); i++)
            if (values.get(i).equals(value))
                return operator == Operator.In;

        return operator != Operator.In;
    }

}
