package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class LogicalModel extends Entity {

    public final Id categoryId;
    public final Id databaseId;
    public final String jsonValue;

    public LogicalModel(Id id, Id categoryId, Id databaseId, String jsonValue) {
        super(id);
        this.categoryId = categoryId;
        this.databaseId = databaseId;
        this.jsonValue = jsonValue;
    }

    public LogicalModelInfo toInfo() {
        return new LogicalModelInfo(id, jsonValue);
    }

}
