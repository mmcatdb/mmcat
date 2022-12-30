package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.integration.Common;

import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class TextProcessor extends Base implements PropertyProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextProcessor.class);

    private static final String TEXT_IRI_PREFIX = Common.CUSTOM_IRI_PREFIX + "text/";

    private static final String ELEMENT = TEXT_IRI_PREFIX + "element";
    private static final String ELEMENT_TO_ATTRIBUTE = TEXT_IRI_PREFIX + "element-to-attribute";
    private static final String LANGUAGE = TEXT_IRI_PREFIX + "language";
    private static final String ELEMENT_TO_LANGUAGE = TEXT_IRI_PREFIX + "element-to-language";
    private static final String VALUE = TEXT_IRI_PREFIX + "value";
    private static final String ELEMENT_TO_VALUE = TEXT_IRI_PREFIX + "element-to-value";

    public TextProcessor(InstanceCategory category) {
        super(category);
    }

    @Override
    public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        final var morphism = findMorphismFromObject(statement.getPredicate().getURI(), resourceObject);

        // TODO - language structure

        return true;
    }

}
