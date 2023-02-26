package cz.cuni.matfyz.server.repository.utils;

import cz.cuni.matfyz.server.entity.Id;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jachym.bartik
 */
public abstract class Utils {

    private Utils() {}

    public static Integer getIntOrNull(int input) {
        return input == 0 ? null : input;
    }

    public static Id getId(ResultSet resultSet, String columnName) throws SQLException {
        return new Id(resultSet.getString(columnName));
    }

    public static void setId(PreparedStatement statement, int position, Id id) throws SQLException {
        //statement.setString(position, id.value);
        try {
            if (id == null) {
                statement.setNull(position, Types.INTEGER);
                return;
            }

            statement.setInt(position, Integer.parseInt(id.toString()));
        }
        catch (NumberFormatException exception) {
            statement.setInt(position, 0);
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    @SuppressWarnings("deprecation")
    public static String toJsonWithoutProperties(Object object, String propertyName) throws JsonProcessingException {
        try {
            final var node = (ObjectNode) mapper.valueToTree(object);
            node.remove(propertyName);
            return node.toString();
        }
        catch (IllegalArgumentException exception) {
            throw new JsonMappingException(exception.getMessage(), exception);
        }
    }

    @SuppressWarnings("deprecation")
    public static String toJsonWithoutProperties(Object object, Collection<String> propertyNames) throws JsonProcessingException {
        try {
            final var node = (ObjectNode) mapper.valueToTree(object);
            node.remove(propertyNames);
            return node.toString();
        }
        catch (IllegalArgumentException exception) {
            throw new JsonMappingException(exception.getMessage(), exception);
        }
    }

}
