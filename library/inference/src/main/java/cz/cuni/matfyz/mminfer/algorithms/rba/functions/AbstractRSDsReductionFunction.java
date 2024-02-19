/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.cuni.matfyz.mminfer.algorithms.rba.functions;

//import cz.cuni.matfyz.mminfer.persister.model.RecordSchemaDescription;
import cz.cuni.matfyz.mminfer.model.RecordSchemaDescription;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractRSDsReductionFunction extends Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription>, Serializable {

}
