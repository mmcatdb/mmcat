/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.algorithms.rba;

import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cz.matfyz.inference.algorithms.rba.functions.AbstractRSDsReductionFunction;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.RecordSchemaDescription;

/**
 *
 * @author pavel.koupil
 */
@Service
public class RecordBasedAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordBasedAlgorithm.class);

    public RecordSchemaDescription process(AbstractInferenceWrapper wrapper, AbstractRSDsReductionFunction merge, Finalize finalize) {
        wrapper.buildSession();
        wrapper.initiateContext();

        try {
            // vytvor universalni pristup nejprve pro JSON
            // teprve potom pridej wrappery, a to do mapovani prvniho
            // STACI MAPOVAT JSON NA RSD, PAK MERGE RSD

            // TOHLE JE MAPPING FAZE
            long start = System.currentTimeMillis();
            JavaRDD<RecordSchemaDescription> allRSDs = wrapper.loadRSDs();
            System.out.println("RESULT_TIME OF MAPPIND AFTER: " + (System.currentTimeMillis() - start) + "ms");

            // PAK FILTER EMPTY VALUE
            // TOHLE JE REDUCE FAZE
            JavaRDD<RecordSchemaDescription> rsds = allRSDs.filter(new FilterInvalidRSDFunction());    // odstrani se prazdne nebo nevalidni RSDs
            System.out.println("RESULT_TIME OF FILTERING AFTER: " + (System.currentTimeMillis() - start) + "ms");

            // PAK FUNKCE MERGE/REDUCE
            //            LOGGER.info("Result is: {}", result);
//            finalize.add(new ProcessedProperty("", result));
            RecordSchemaDescription rsd = rsds.reduce(merge);
            System.out.println("RESULT_TIME OF MERGE AFTER: " + (System.currentTimeMillis() - start) + "ms");
            return rsd;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        } finally {
            wrapper.stopSession();
        }
    }

}
