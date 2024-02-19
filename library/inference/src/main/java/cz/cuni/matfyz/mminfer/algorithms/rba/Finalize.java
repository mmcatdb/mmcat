/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package cz.cuni.matfyz.mminfer.algorithms.rba;

//import cz.cuni.matfyz.mminfer.model.ProcessedProperty;
import cz.cuni.matfyz.mminfer.model.ProcessedProperty;
import cz.cuni.matfyz.mminfer.model.RecordSchemaDescription;
//import cz.cuni.matfyz.mminfer.persister.model.RecordSchemaDescription;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public interface Finalize {

	public void add(ProcessedProperty data);

	public void add(List<ProcessedProperty> data);

	public RecordSchemaDescription process();

}
