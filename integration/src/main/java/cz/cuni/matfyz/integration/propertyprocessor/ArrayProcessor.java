package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.category.Morphism.Tag;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.integration.utils.Constants;

import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class ArrayProcessor extends Base implements PropertyProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayProcessor.class);

    private static final String ARRAY_IRI_PREFIX = Constants.CUSTOM_IRI_PREFIX + "array/";

    private static final String ELEMENT_TO_COD_SUFFIX = "/_array";
    private static final String ELEMENT = ARRAY_IRI_PREFIX + "element";
    private static final String ELEMENT_TO_DOM = ARRAY_IRI_PREFIX + "element-to-dom";
    private static final String INDEX = ARRAY_IRI_PREFIX + "index";
    private static final String ELEMENT_TO_INDEX = ARRAY_IRI_PREFIX + "element-to-index";

    public ArrayProcessor(InstanceCategory category) {
        super(category);
    }

    @Override
    public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        LOGGER.warn("[Array]: {}", statement);

        final var morphism = finder.findFromObject(resourceObject, statement.getPredicate().getURI() + ELEMENT_TO_COD_SUFFIX);
        if (morphism == null)
            return false;
        if (!morphism.cod().schemaObject.ids().isValue())
            return false;

        final var elementToCod = morphism.lastBase();
        if (!elementToCod.schemaMorphism.hasTag(Tag.role))
            return false;

        final var element = elementToCod.dom();
        if (!element.schemaObject.pimIri.equals(ELEMENT))
            return false;

        final var cod = elementToCod.cod();

        final var elementToIndex = finder.findDirectFromObject(element, ELEMENT_TO_INDEX);
        final var index = elementToIndex.cod();
        if (!index.schemaObject.pimIri.equals(INDEX))
            return false;

        final var elementToDom = finder.findDirectFromObject(element, ELEMENT_TO_DOM);
        if (!elementToDom.schemaMorphism.hasTag(Tag.role))
            return false;

        final var dom = elementToDom.cod();
    
        // TODO - how to make arrays in RDF?

        return false;
    }

}
