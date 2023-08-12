package cz.matfyz.integration.propertyprocessor;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.integration.exception.RDFNodeException;
import cz.matfyz.integration.utils.MorphismFinder;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

/**
 * @author jachym.bartik
 */
public class PropertyProcessorBase {

    protected final InstanceCategory category;
    protected final MorphismFinder finder;
    
    protected PropertyProcessorBase(InstanceCategory category) {
        this.category = category;
        this.finder = new MorphismFinder(category);
        this.typeProcessors = defineTypeProcessors(category);
    }

    private static List<? extends TypeProcessor> defineTypeProcessors(InstanceCategory category) {
        final List<TypeProcessor> output = new ArrayList<>();

        output.add(new TextProcessor(category));
        output.add(new StringProcessor(category));

        return output;
    }

    private final List<? extends TypeProcessor> typeProcessors;

    protected DomainRow createTypeRow(RDFNode statementObject, InstanceObject attributeObject) {
        for (final var processor : typeProcessors) {
            final var row = processor.tryCreateTypeRow(statementObject, attributeObject);
            if (row != null)
                return row;
        }

        throw RDFNodeException.unprocessable(statementObject.asNode());
    }
    
}
