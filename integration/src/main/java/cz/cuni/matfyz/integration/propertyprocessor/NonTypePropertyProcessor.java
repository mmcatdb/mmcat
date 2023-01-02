package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
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
    public boolean tryProcessProperty(Statement statement, InstanceObject object, DomainRow row) {
        final var morphism = finder.findFromObject(object, statement.getPredicate().getURI());
        if (morphism == null)
            return false;
        if (!morphism.cod().schemaObject.ids().isValue())
            return false;

        final var statementObject = statement.getObject();
        if (statementObject.isLiteral())
            return tryAddLiteral(statementObject.asLiteral(), object, row, morphism);
        if (statementObject.isResource())
            return tryAddResource(statementObject.asResource(), object, row, morphism);

        return false;
    }

    private boolean tryAddLiteral(Literal literal, InstanceObject object, DomainRow row, InstanceMorphism morphism) {
        final var valueSuperId = SuperIdWithValues.fromEmptySignature(literal.getLexicalForm());
        object.getOrCreateRowWithMorphism(valueSuperId, row, morphism);

        return true;
    }

    private boolean tryAddResource(Resource resource, InstanceObject object, DomainRow row, InstanceMorphism morphism) {
        final var valueSuperId = SuperIdWithValues.fromEmptySignature(resource.getURI());
        object.getOrCreateRowWithMorphism(valueSuperId, row, morphism);

        return true;
    }
    
}
