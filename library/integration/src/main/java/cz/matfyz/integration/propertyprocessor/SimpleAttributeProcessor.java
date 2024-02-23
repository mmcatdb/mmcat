package cz.matfyz.integration.propertyprocessor;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.integration.utils.IsaMorphismCreator;

import org.apache.jena.rdf.model.Statement;

/**
 * @author jachym.bartik
 */
public class SimpleAttributeProcessor extends PropertyProcessorBase implements PropertyProcessor {

    public SimpleAttributeProcessor(InstanceCategory category) {
        super(category);
    }

    @Override public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        final var resourceToProperty = finder.tryFindFromObject(resourceObject, statement.getPredicate().getURI());
        if (resourceToProperty == null)
            return false;

        final var propertyRow = createTypeRow(statement.getObject(), resourceToProperty.cod());
        // This should be merge-safe.
        IsaMorphismCreator.connectRowWithIsaMorphism(propertyRow, resourceRow, resourceToProperty);
        //final var lastIsaResourceRow = IsaMorphismCreator.getOrCreateLastIsaRow(resourceRow, resourceToProperty);

        return true;
    }

}
