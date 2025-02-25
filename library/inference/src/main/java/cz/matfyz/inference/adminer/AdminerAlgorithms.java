package cz.matfyz.inference.adminer;

import java.util.List;

import cz.matfyz.core.record.AdminerFilter;

public class AdminerAlgorithms {
    private AdminerAlgorithms() {}

    /**
     * Parses a numeric value from a given string.
     * If the string represents a valid number, it returns the parsed {@code Double}.
     * Otherwise, it returns {@code null}.
     *
     * @param str the string to be parsed
     * @return the parsed {@code Double} value if valid, or {@code null} if the input is {@code null} or not a valid number
     */
    public static Double parseNumeric(String str) {
        if (str == null) {
            return null;
        }

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void appendPropertyName(StringBuilder whereClause, String name, String propertyName, Double doubleValue, AdminerAlgorithmsInterface algorithms) {
        if (doubleValue != null && algorithms instanceof Neo4jAlgorithms) {
            whereClause.append("toFloat(");
        }

        if (name != null) {
            whereClause.append(name)
                .append(".");
        }
        whereClause.append(propertyName);

        if (doubleValue != null) {
            if (algorithms instanceof Neo4jAlgorithms) {
                whereClause.append(")");
            } else if (algorithms instanceof PostgreSQLAlgorithms) {
                whereClause.append("::NUMERIC");
            }
        }
    }

    private static void appendOperator(StringBuilder whereClause, String operator) {
        whereClause
            .append(" ")
            .append(operator)
            .append(" ");
    }

    private static void appendPropertyValue(StringBuilder whereClause, String propertyValue, String operator, Double doubleValue, AdminerAlgorithmsInterface algorithms) {
        if (operator.equals("IN") && algorithms instanceof Neo4jAlgorithms) {
            whereClause
                .append("[")
                .append(propertyValue)
                .append("]");
        } else if ((operator.equals("IN") || operator.equals("NOT IN")) && algorithms instanceof PostgreSQLAlgorithms) {
            whereClause
                .append("(")
                .append(propertyValue)
                .append(")");
        } else if (!algorithms.getUnaryOperators().contains(operator)) {
            if (doubleValue != null) {
                whereClause
                    .append(doubleValue);
            } else {
                whereClause
                    .append("'")
                    .append(propertyValue)
                    .append("'");
            }
        }
    }

    /**
     * Constructs a WHERE clause based on a list of filters.
     *
     * @param filters The filters to apply.
     * @param name The alias assigned to the graph element in the query.
     *             For graph databases: 'a' for nodes, 'r' for relationships.
     *             For non-graph databases: {@code null}.
     * @return A WHERE clause as a {@link String}.
     */
    public static String createWhereClause(AdminerAlgorithmsInterface algorithms, List<AdminerFilter> filters, String name) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder("WHERE ");

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            String operator = algorithms.getOperators().get(filter.operator());

            if (i != 0) {
                whereClause.append(" AND ");
            }

            Double doubleValue = AdminerAlgorithms.parseNumeric(filter.propertyValue());

            appendPropertyName(whereClause, name, filter.propertyName(), doubleValue, algorithms);

            appendOperator(whereClause, operator);

            appendPropertyValue(whereClause, filter.propertyValue(), operator, doubleValue, algorithms);
        }

        return whereClause.toString();
    }
}
