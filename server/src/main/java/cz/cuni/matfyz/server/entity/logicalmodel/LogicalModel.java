package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.Entity;

/**
 * @author jachym.bartik
 */
public class LogicalModel extends Entity {

    public final int categoryId;
    public final int databaseId;
    public final String jsonValue;

    public LogicalModel(Integer id, int categoryId, int databaseId, String jsonValue) {
        super(id);
        this.categoryId = categoryId;
        this.databaseId = databaseId;
        this.jsonValue = jsonValue;
    }

    public LogicalModelInfo toInfo() {
        return new LogicalModelInfo(id, categoryId, jsonValue);
    }

}
