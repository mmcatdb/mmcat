package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;

import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class NonTypePropertyProcessor extends Base implements PropertyProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonTypePropertyProcessor.class);

    public NonTypePropertyProcessor(InstanceCategory category) {
        super(category);
    }

    @Override
    public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        final var morphism = findMorphismFromObject(statement.getPredicate().getURI(), resourceObject);
        if (morphism == null)
            return false;
        if (!morphism.cod().schemaObject.ids().isValue())
            return false;

        final var literalValue = statement.getObject().asLiteral();
        LOGGER.info("[Value]: {}", literalValue.getLexicalForm());
        //LOGGER.warn(literalValue.getString());

        final var valueSuperId = new SuperIdWithValues.Builder().add(Signature.createEmpty(), literalValue.getLexicalForm()).build();
        resourceObject.getOrCreateRowWithMorphism(valueSuperId, resourceRow, morphism);
        
        return true;
    }
    
}
