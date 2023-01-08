package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.integration.utils.Constants;

import org.apache.jena.rdf.model.RDFNode;

/**
 * @author jachym.bartik
 */
public class TextProcessor extends TypeProcessorBase implements TypeProcessor {

    private static final String TEXT_IRI_PREFIX = Constants.CUSTOM_IRI_PREFIX + "text/";

    //private static final String LANGUAGE = TEXT_IRI_PREFIX + "language";
    private static final String ATTRIBUTE_TO_LANGUAGE = TEXT_IRI_PREFIX + "attribute-to-language";
    //private static final String VALUE = TEXT_IRI_PREFIX + "value";
    private static final String ATTRIBUTE_TO_VALUE = TEXT_IRI_PREFIX + "attribute-to-value";

    public TextProcessor(InstanceCategory category) {
        super(category);
    }

    @Override
    public DomainRow tryCreateTypeRow(RDFNode statementObject, InstanceObject attributeObject) {
        // TODO check if the object ids include only language and value
        if (!attributeObject.ids().isSignatures())
            return null;

        if (!statementObject.isLiteral())
            return null;

        final var literal = statementObject.asLiteral();
        final var languageString = literal.getLanguage();
        if (languageString.isEmpty())
            return null;

        final var valueString = literal.getLexicalForm();

        final var attributeToLanguage = finder.findDirectFromObject(attributeObject, ATTRIBUTE_TO_LANGUAGE);
        final var attributeToValue = finder.findDirectFromObject(attributeObject, ATTRIBUTE_TO_VALUE);
        if (attributeToLanguage == null || attributeToValue == null)
            return null;

        final var attributeSuperId = new SuperIdWithValues.Builder()
            .add(attributeToLanguage.signature(), languageString)
            .add(attributeToValue.signature(), valueString)
            .build();

        final var alreadyExistingAttributeRow = attributeObject.getRowById(attributeSuperId);
        if (alreadyExistingAttributeRow != null)
            return alreadyExistingAttributeRow;

        // TODO this should be handled by the merger algorithm but isn't ...
        final var attributeRow = attributeObject.getOrCreateRow(attributeSuperId);
        InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.fromEmptySignature(languageString), attributeRow, attributeToLanguage);
        InstanceObject.getOrCreateRowWithBaseMorphism(SuperIdWithValues.fromEmptySignature(valueString), attributeRow, attributeToValue);

        return attributeRow;
    }

}
