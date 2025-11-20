package cz.matfyz.server.utils;

import cz.matfyz.server.exception.RepositoryException;
import cz.matfyz.server.utils.entity.Id;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class Utils {

    private Utils() {}

    public static @Nullable Id getId(ResultSet resultSet, String columnName) throws SQLException {
        final var idString = resultSet.getString(columnName);
        return idString == null ? null : new Id(idString);
    }

    public static void setId(PreparedStatement statement, int position, @Nullable Id id) throws SQLException {
        if (id == null)
            statement.setNull(position, Types.OTHER);
        else
            statement.setObject(position, id.toUUID());
    }

    public static void setIds(PreparedStatement statement, int position, List<Id> ids) throws SQLException {
        final var uuids = ids.stream().map(Id::toUUID).toArray(UUID[]::new);
        statement.setArray(position, statement.getConnection().createArrayOf("UUID", uuids));
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
