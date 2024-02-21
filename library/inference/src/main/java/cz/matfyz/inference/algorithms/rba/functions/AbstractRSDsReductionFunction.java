/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.matfyz.inference.algorithms.rba.functions;

import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;

import cz.matfyz.inference.model.RecordSchemaDescription;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractRSDsReductionFunction extends Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription>, Serializable {

}
