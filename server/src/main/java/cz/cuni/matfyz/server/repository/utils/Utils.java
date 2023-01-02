package cz.cuni.matfyz.server.repository.utils;

import cz.cuni.matfyz.server.entity.Id;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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

            statement.setInt(position, Integer.parseInt(id.value));
        }
        catch (NumberFormatException exception) {
            statement.setInt(position, 0);
        }
    }

}
