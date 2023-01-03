package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;
import cz.cuni.matfyz.integration.utils.Constants;
import cz.cuni.matfyz.integration.utils.IsaMorphismCreator;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class TextProcessor extends Base implements PropertyProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextProcessor.class);

    private static final String TEXT_IRI_PREFIX = Constants.CUSTOM_IRI_PREFIX + "text/";

    //private static final String ELEMENT = TEXT_IRI_PREFIX + "element";
    private static final String ELEMENT_TO_ATTRIBUTE = TEXT_IRI_PREFIX + "element-to-attribute";
    //private static final String LANGUAGE = TEXT_IRI_PREFIX + "language";
    private static final String ELEMENT_TO_LANGUAGE = TEXT_IRI_PREFIX + "element-to-language";
    //private static final String VALUE = TEXT_IRI_PREFIX + "value";
    private static final String ELEMENT_TO_VALUE = TEXT_IRI_PREFIX + "element-to-value";

    public TextProcessor(InstanceCategory category) {
        super(category);
    }

    @Override
    public boolean tryProcessProperty(Statement statement, InstanceObject resourceObject, DomainRow resourceRow) {
        final var resourceToAttribute = finder.findFromObject(resourceObject, statement.getPredicate().getURI());
        if (resourceToAttribute == null)
            return false;
        if (!resourceToAttribute.cod().schemaObject.ids().isGenerated())
            return false;

        final var statementObject = statement.getObject();
        if (statementObject.isLiteral())
            return tryAddLanguageText(statementObject.asLiteral(), resourceRow, resourceToAttribute);

        return false;
    }

    private boolean tryAddLanguageText(Literal literal, DomainRow resourceRow, InstanceMorphism resourceToAttribute) {
        if (literal.getLanguage().isEmpty())
            return false;
        
        final var attributeRow = getOrCreateAttributeRow(resourceRow, resourceToAttribute);

        final var elementToAttribute = finder.findDirectToObject(resourceToAttribute.cod(), ELEMENT_TO_ATTRIBUTE);
        if (elementToAttribute == null)
            return false;

        final var element = elementToAttribute.dom();
        if (element == null)
            return false;

        final var elementToLanguage = finder.findDirectFromObject(element, ELEMENT_TO_LANGUAGE);
        final var elementToValue = finder.findDirectFromObject(element, ELEMENT_TO_VALUE);
        if (elementToLanguage == null || elementToValue == null)
            return false;

        final var newElementSuperId = new SuperIdWithValues.Builder()
            .add(elementToAttribute.signature(), attributeRow.superId.getValue(Signature.createEmpty()))
            .add(elementToLanguage.signature(), literal.getLanguage())
            .add(elementToValue.signature(), literal.getLexicalForm())
            .build();

        final var elementRow = InstanceObject.getOrCreateRowWithBaseMorphism(newElementSuperId, attributeRow, elementToAttribute.dual());
        InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.fromEmptySignature(literal.getLanguage()), elementRow, elementToLanguage);
        InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.fromEmptySignature(literal.getLexicalForm()), elementRow, elementToValue);

        return true;
    }

    private DomainRow getOrCreateAttributeRow(DomainRow resourceRow, InstanceMorphism resourceToAttribute) {
        final var mapping = resourceRow.getMappingsFromForMorphism(resourceToAttribute).stream().findFirst();
        if (mapping.isPresent())
            return mapping.get().codomainRow();

        final var generatedSuperId = SuperIdWithValues.fromEmptySignature(UniqueIdProvider.getNext());
        return IsaMorphismCreator.getOrCreateRowForIsaMorphism(generatedSuperId, resourceRow, resourceToAttribute);
    }

}
