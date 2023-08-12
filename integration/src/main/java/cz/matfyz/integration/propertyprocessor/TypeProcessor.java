package cz.matfyz.integration.propertyprocessor;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceObject;

import org.apache.jena.rdf.model.RDFNode;

/**
 * @author jachym.bartik
 */
public interface TypeProcessor {
    
    public DomainRow tryCreateTypeRow(RDFNode statementObject, InstanceObject attributeObject);

}
