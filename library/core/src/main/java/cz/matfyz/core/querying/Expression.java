package cz.matfyz.core.querying;

import java.util.List;

/**
 * Something that can be evaluated to a value.
 * Variable, Constant, or FunctionExpression.
 */
public interface Expression {

    public record Constant(
        String value
    ) implements Expression {

        @Override public String toString() {
            return value;
        }

    }

    public record FunctionExpression(
        Operator operator,
        List<Expression> arguments
    ) implements Expression {

        public FunctionExpression(Operator operator, Expression... arguments) {
            this(operator, List.of(arguments));
        }

        @Override public String toString() {
            final var builder = new StringBuilder();
            builder.append("[").append(operator).append("](");

            builder.append(arguments.get(0));
            for (int i = 1; i < arguments.size(); i++)
                builder.append(", ").append(arguments.get(i));

            builder.append(")");

            return builder.toString();
        }

    }

    // No, separate enums won't work here because even though they can extend interfaces, they can't reimplement the Comparable<Operator> interface. And that's the one we needed for any kind of set or map.
    // And yes, this looks horrendous. But it's the only way to make it work. Thanks, java.

    enum OP {
        Comparison,
        Aggregation,
        Set;
    }

    enum Operator {

        /**
         * Expected inputs:
         *  - ?a <operator> ?b
         *  - ?a <operator> "value"
         */
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
        NotIn           (OP.Set);

        private OP type;

        private Operator(OP type) {
            this.type = type;
        }

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

    }

}
