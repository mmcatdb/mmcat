package cz.matfyz.abstractwrappers.collector;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;

/**
 * Class which represents unified API for communication with all wrappers from server module
 *
 * TODO: maybe merge with / outsource part of functionality to pullwrapper or controlwrapper?
 */
public interface CollectorWrapper {


    /**
     * Method which is executed by QueryScheduler to compute statistical result of query over this wrapper
     * @param query inputted
     * @return instance of DataModel which contains all measured data
     * @throws WrapperException when some problem occur during process, message of this exception is saved as a result to execution if some error is thrown during evaluation
     */
    public abstract DataModel executeQuery(String query) throws WrapperException;
}
