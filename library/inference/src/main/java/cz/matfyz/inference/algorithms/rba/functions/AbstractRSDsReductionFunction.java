package cz.matfyz.inference.algorithms.rba.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.Function2;

public interface AbstractRSDsReductionFunction extends Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription> {

}
