package cz.matfyz.abstractwrappers.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

public abstract class AbstractComponent {
    private final WrapperExceptionsFactory _exceptionsFactory;

    public AbstractComponent(WrapperExceptionsFactory exceptionsFactory) {
        _exceptionsFactory = exceptionsFactory;
    }

    protected WrapperExceptionsFactory getExceptionsFactory() {
        return _exceptionsFactory;
    }

    protected <TFactory> TFactory getExceptionsFactory(Class<TFactory> factoryClass) {
        return factoryClass.cast(_exceptionsFactory);
    }
}
