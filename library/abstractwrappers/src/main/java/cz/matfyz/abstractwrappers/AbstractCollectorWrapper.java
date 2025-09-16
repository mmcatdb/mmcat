package cz.matfyz.abstractwrappers;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;

/**
 * Class which represents unified API for communication with all wrappers from server module
 */
public interface AbstractCollectorWrapper {
    /**
     * Method which is executed by QueryScheduler to compute statistical result of query over this wrapper
     * @param query inputted
     * @return instance of DataModel which contains all measured data
     * @throws WrapperException when some problem occur during process, message of this exception is saved as a result to execution if some error is thrown during evaluation
     */
    public abstract DataModel executeQuery(QueryContent query) throws WrapperException;
}
