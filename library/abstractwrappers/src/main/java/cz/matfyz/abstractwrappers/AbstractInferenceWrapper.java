/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.matfyz.abstractwrappers;


import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;

/**
 *
 * @author pavel.koupil
 */

public abstract class AbstractInferenceWrapper {

    public String kindName;

    public abstract void buildSession();

    public abstract void stopSession();

    public abstract JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData);

    public abstract void initiateContext();

    public abstract JavaRDD<RecordSchemaDescription> loadRSDs();

    public abstract JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs();

}