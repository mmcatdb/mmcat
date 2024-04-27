package cz.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;

interface DatabaseFunction<T> {

    T execute(Connection connection) throws SQLException, JsonProcessingException;

}
