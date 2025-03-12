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

    private static void appendIdPropertyName(StringBuilder whereClause, String alias, boolean startNodeId, boolean endNodeId) {
        whereClause.append("id(");

        if (startNodeId) {
            whereClause.append("startNode(");
        } else if (endNodeId) {
            whereClause.append("endNode(");
        }

        whereClause
            .append(alias);

        if (startNodeId || endNodeId)
            whereClause.append(")");

        whereClause
            .append(") ");
    }

    private static void appendPropertyName(StringBuilder whereClause, String alias, String propertyName, Double doubleValue, AdminerAlgorithmsInterface algorithms) {
        boolean startNodeId = propertyName.equals("startNodeId");
        boolean endNodeId = propertyName.equals("endNodeId");

        if (algorithms instanceof Neo4jAlgorithms && (propertyName.equals("elementId") || startNodeId || endNodeId)) {
            appendIdPropertyName(whereClause, alias, startNodeId, endNodeId);

            return;
        }

        if (doubleValue != null && algorithms instanceof Neo4jAlgorithms) {
            whereClause.append("toFloat(");
        }

        if (alias != null) {
            whereClause.append(alias)
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
     * @param alias The alias assigned to the graph element in the query.
     *             For graph databases: 'n' for nodes, 'r' for relationships.
     *             For non-graph databases: {@code null}.
     * @return A WHERE clause as a {@link String}.
     */
    public static String createWhereClause(AdminerAlgorithmsInterface algorithms, List<AdminerFilter> filters, String alias) {
        if ((filters == null || filters.isEmpty())) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder();

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            String operator = algorithms.getOperators().get(filter.operator());

            if (i != 0) {
                whereClause.append(" AND ");
            }

            Double doubleValue = AdminerAlgorithms.parseNumeric(filter.propertyValue());
            String propertyName = filter.propertyName();

            if (algorithms instanceof Neo4jAlgorithms && propertyName.startsWith("properties.")) {
                propertyName = propertyName.substring(11); // Remove prefix 'properties.'
            }

            appendPropertyName(whereClause, alias, propertyName, doubleValue, algorithms);

            appendOperator(whereClause, operator);

            appendPropertyValue(whereClause, filter.propertyValue(), operator, doubleValue, algorithms);
        }

        return whereClause.toString();
    }
}
