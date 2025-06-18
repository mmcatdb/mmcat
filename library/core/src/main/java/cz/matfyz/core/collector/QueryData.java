package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main class holding gathered statistical data which can be eventually transformed to json for persistent storage
 */
class QueryData {

    /** Field containing dbType for which is this record relevant */
    @JsonProperty("systemName")
    private final String _systemName;

    /** Field containing name of dataset */
    @JsonProperty("databaseName")
    private final String _databaseName;

    /** Field containing query for which these statistical data were gathered */
    @JsonProperty("query")
    private final String _query;

    @JsonProperty("databaseData")
    private final DatabaseData _databaseData;

    @JsonProperty("resultData")
    private final ResultData _resultData;

    public QueryData(String query, String systemName, String databaseName) {
        _query = query;
        _systemName = systemName;
        _databaseName = databaseName;

        _databaseData = new DatabaseData();
        _resultData = new ResultData();
    }

    @JsonIgnore
    public DatabaseData getDatabaseData() {
        return _databaseData;
    }
    @JsonIgnore
    public ResultData getResultData() {
        return _resultData;
    }
}
