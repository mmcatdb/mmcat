package cz.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface DatabaseGetBooleanFunction {

    void execute(Connection connection, BooleanOutput output) throws SQLException, JsonProcessingException;

}
