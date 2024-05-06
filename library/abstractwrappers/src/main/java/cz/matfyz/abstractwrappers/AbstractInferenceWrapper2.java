/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.matfyz.abstractwrappers;

import cz.matfyz.core.rsd2.RawProperty;
import cz.matfyz.core.rsd2.RecordSchemaDescription;
import cz.matfyz.core.rsd2.PropertyHeuristics;
import cz.matfyz.core.rsd2.Share;
import java.util.List;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

/**
 *
 * @author pavel.koupil
 */
public abstract class AbstractInferenceWrapper2 {

	public String kindName;

	public abstract void buildSession();

	public abstract void stopSession();

	public abstract JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData);
	
	public abstract JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema();
        
        public abstract JavaPairRDD<String, PropertyHeuristics> loadPropertyData();

	public abstract void initiateContext();

	public abstract JavaRDD<RecordSchemaDescription> loadRSDs();

	public abstract JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs();
        
        public abstract AbstractInferenceWrapper2 copy();

}
