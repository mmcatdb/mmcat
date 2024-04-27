package cz.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface DatabaseGetArrayFunction<T> {

    void execute(Connection connection, ArrayOutput<T> output) throws SQLException, JsonProcessingException;

}
