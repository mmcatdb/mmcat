package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.Share;
import org.apache.spark.api.java.function.Function2;

public class ReduceRawPropertiesFunction implements Function2<Share, Share, Share> {

    @Override
    public Share call(Share share1, Share share2) throws Exception {
        share1.setFirst(share1.getFirst() + share2.getFirst());
        share1.setTotal(share1.getTotal() + share2.getTotal());
        return share1;
    }

}
