package cz.matfyz.wrappermongodb.inference.functions;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.Share;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class MongoRawPropertyPairFunction implements PairFunction<RawProperty, RawProperty, Share> {

    @Override
    public Tuple2<RawProperty, Share> call(RawProperty t) {
        return new Tuple2<>(t, new Share(t.getCount(), t.getFirst()));
    }

}
