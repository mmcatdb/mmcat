package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.database.DatabaseView;

/**
 * @author jachym.bartik
 */
public class LogicalModelView extends Entity {

    public final int categoryId;
    public final DatabaseView databaseView;
    public final String jsonValue;

    public LogicalModelView(LogicalModel model, DatabaseView databaseView) {
        super(model.id);
        this.categoryId = model.categoryId;
        this.databaseView = databaseView;
        this.jsonValue = model.jsonValue;
    }

}
