package cz.matfyz.inference.algorithms.miner.functions;

import cz.matfyz.core.rsd.ProcessedProperty;

public enum ProcessedPropertyFactory {
    INSTANCE;

    public ProcessedProperty empty() {
        return null;
    }

}
