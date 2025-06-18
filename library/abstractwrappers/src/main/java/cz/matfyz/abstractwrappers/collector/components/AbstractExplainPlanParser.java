package cz.matfyz.abstractwrappers.collector.components;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.abstractwrappers.exception.collector.WrapperUnsupportedOperationException;

public abstract class AbstractExplainPlanParser<TPlan> extends AbstractComponent {

    public AbstractExplainPlanParser(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    public abstract void parsePlan(TPlan plan, DataModel model) throws ParseException, WrapperUnsupportedOperationException;
}
