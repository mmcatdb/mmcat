package cz.matfyz.abstractwrappers.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.core.collector.queryresult.CachedResult;
import cz.matfyz.core.collector.queryresult.ConsumedResult;

public abstract class AbstractQueryResultParser<TResult> extends AbstractComponent {

    public AbstractQueryResultParser(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    public abstract CachedResult parseResultAndCache(TResult result) throws ParseException;

    public abstract ConsumedResult parseResultAndConsume(TResult result) throws ParseException;

}
