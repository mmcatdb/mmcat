package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import org.apache.spark.api.java.function.Function2;

public interface AbstractSeqFunction extends Function2<ProcessedProperty, Iterable<ProcessedProperty>, ProcessedProperty> {

}
