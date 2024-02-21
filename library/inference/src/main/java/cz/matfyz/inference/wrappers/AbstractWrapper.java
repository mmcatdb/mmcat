/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.matfyz.inference.wrappers;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import cz.matfyz.inference.model.RawProperty;
import cz.matfyz.inference.model.RecordSchemaDescription;
import cz.matfyz.inference.model.Share;

/**
 *
 * @author pavel.koupil
 */
public abstract class AbstractWrapper {

	public String kindName;

	public abstract void buildSession();

	public abstract void stopSession();

	public abstract JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData);

	public abstract void initiateContext();

	public abstract JavaRDD<RecordSchemaDescription> loadRSDs();

	public abstract JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs();

}
