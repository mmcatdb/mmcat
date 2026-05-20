package cz.matfyz.abstractwrappers;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;

public interface AbstractCollectorWrapper {

    /**
     * Method which is executed by QueryScheduler to compute statistical result of query over this wrapper
     * @param query inputted query
     * @return instance of DataModel which contains all measured data
     */
    public abstract DataModel executeQuery(QueryContent query) throws WrapperException;

}
