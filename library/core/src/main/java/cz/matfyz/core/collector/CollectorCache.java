package cz.matfyz.core.collector;

import java.util.HashMap;
import java.util.Map;

public class CollectorCache {

    public final Map<String, DataModel.DatabaseData> databaseData = new HashMap<>();

    // TODO: Design a system for using results of previous queries in "similar" future queries
    // e.g. with a map
    // public final Map<Query, DataModel> queryToResult = ...;

}
