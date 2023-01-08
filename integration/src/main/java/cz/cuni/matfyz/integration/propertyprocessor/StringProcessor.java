package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * @author jachym.bartik
 */
public class StringProcessor extends TypeProcessorBase implements TypeProcessor {

    public StringProcessor(InstanceCategory category) {
        super(category);
    }

    @Override
    public DomainRow tryCreateTypeRow(RDFNode statementObject, InstanceObject attributeObject) {
        if (!attributeObject.ids().isValue())
            return null;

        if (statementObject.isLiteral())
            return createLiteral(statementObject.asLiteral(), attributeObject);
        if (statementObject.isResource())
            return createResource(statementObject.asResource(), attributeObject);

        return null;
    }

    private static DomainRow createLiteral(Literal literal, InstanceObject attribuObject) {
        final var valueSuperId = SuperIdWithValues.fromEmptySignature(literal.getLexicalForm());
        return attribuObject.getOrCreateRow(valueSuperId);
    }

    private static DomainRow createResource(Resource resource, InstanceObject attribuObject) {
        final var valueSuperId = SuperIdWithValues.fromEmptySignature(resource.getURI());
        return attribuObject.getOrCreateRow(valueSuperId);
    }
    
}
