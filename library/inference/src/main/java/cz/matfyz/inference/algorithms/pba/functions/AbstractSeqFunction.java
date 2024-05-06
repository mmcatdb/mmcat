/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.ProcessedProperty;
import java.io.Serializable;
import org.apache.spark.api.java.function.Function2;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractSeqFunction extends Function2<ProcessedProperty, Iterable<ProcessedProperty>, ProcessedProperty>, Serializable {

}
