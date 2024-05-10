package cz.matfyz.inference.algorithms.miner.functions;

import cz.matfyz.core.rsd.Share;
import org.apache.spark.api.java.function.Function2;

public class ReduceRawPropertiesFunction implements Function2<Share, Share, Share> {

    @Override
    public Share call(Share a, Share b) throws Exception {
        return a.add(b);
    }

}
