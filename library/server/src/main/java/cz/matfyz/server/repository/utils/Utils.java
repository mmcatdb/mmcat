package cz.matfyz.server.repository.utils;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.exception.RepositoryException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class Utils {

    private Utils() {}

    public static Integer getIntOrNull(int input) {
        return input == 0 ? null : input;
    }

    public static Id getId(ResultSet resultSet, String columnName) throws SQLException {
        return new Id(resultSet.getString(columnName));
    }

    public static @Nullable Id getIdOrNull(ResultSet resultSet, String columnName) throws SQLException {
        final var idString = resultSet.getString(columnName);
        return idString == null ? null : new Id(idString);
    }

    public static void setId(PreparedStatement statement, int position, @Nullable Id id, boolean isUuid) throws SQLException {
        if (id == null)
            statement.setNull(position, isUuid ? Types.OTHER : Types.INTEGER);
        else
            setId(statement, position, id);
    }

    public static void setId(PreparedStatement statement, int position, Id id) throws SQLException {
        if (id.isUuid()) {
            statement.setObject(position, UUID.fromString(id.toString()));
            return;
        }

        try {
            //statement.setString(position, id.value);
            statement.setInt(position, Integer.parseInt(id.toString()));
        }
        catch (NumberFormatException e) {
            statement.setInt(position, 0);
        }
    }

    public static void executeChecked(PreparedStatement statement) throws SQLException {
        final int affectedRows = statement.executeUpdate();
        if (affectedRows == 0)
            throw RepositoryException.nothingUpdated();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

}
