package cz.matfyz.integration.propertyprocessor;

import cz.matfyz.core.category.Morphism.Tag;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.instance.SuperIdWithValues;
import cz.matfyz.core.utils.Config;
import cz.matfyz.core.utils.UniqueIdProvider;
import cz.matfyz.integration.utils.IsaMorphismCreator;

import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class ArrayProcessor extends PropertyProcessorBase implements PropertyProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayProcessor.class);

    private static final String ARRAY_IRI_PREFIX = Config.GLOBAL.get("integration.iriPrefix") + "array/";

    private static final String DOM_TO_ELEMENT_SUFFIX = "/_array";
    private static final String ELEMENT = ARRAY_IRI_PREFIX + "element";
    private static final String ELEMENT_TO_COD = ARRAY_IRI_PREFIX + "element-to-cod";
    private static final String INDEX = ARRAY_IRI_PREFIX + "index";
    private static final String ELEMENT_TO_INDEX = ARRAY_IRI_PREFIX + "element-to-index";

    public ArrayProcessor(InstanceCategory category) {
        super(category);
    }

    @Override public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        LOGGER.warn("[Array]: {}", statement);

        final var elementToDom = finder.tryFindBaseByPimIri(statement.getPredicate().getURI() + DOM_TO_ELEMENT_SUFFIX);
        if (elementToDom == null || !elementToDom.schemaMorphism.hasTag(Tag.role))
            return false;

        final var resourceToDom = finder.tryFindFromObjectToObject(resourceObject, elementToDom.cod());
        if (resourceToDom == null)
            return false;

        final var element = elementToDom.dom();
        if (!element.schemaObject.ids().isGenerated())
            return false;
        if (!element.schemaObject.pimIri.equals(ELEMENT))
            return false;

        final var elementToCod = finder.tryFindDirectFromObject(element, ELEMENT_TO_COD);
        if (elementToCod == null)
            return false;
        if (!elementToCod.schemaMorphism.hasTag(Tag.role))
            return false;

        final var elementToIndex = finder.tryFindDirectFromObject(element, ELEMENT_TO_INDEX);
        if (elementToIndex == null)
            return false;
        final var index = elementToIndex.cod();
        if (!index.schemaObject.pimIri.equals(INDEX))
            return false;

        final var domRow = IsaMorphismCreator.getOrCreateLastIsaRow(resourceRow, resourceToDom);
        final var newElementSuperId = SuperIdWithValues.fromEmptySignature(UniqueIdProvider.getNext());

        final var elementRow = elementToDom.dom().getOrCreateRow(newElementSuperId);
        
        final var codRow = createTypeRow(statement.getObject(), elementToCod.cod());
        InstanceObject.connectRowWithBaseMorphism(codRow, elementRow, elementToCod);
        
        final var indexValue = domRow.getMappingsToForMorphism(elementToDom).size() - 1; // We index from zero.
        InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.fromEmptySignature("" + indexValue), elementRow, elementToIndex);

        return true;
    }

}
