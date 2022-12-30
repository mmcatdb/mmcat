package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceObject;

import org.apache.jena.rdf.model.Statement;

/**
 * @author jachym.bartik
 */
public interface PropertyProcessor {
    
    public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow);

}
