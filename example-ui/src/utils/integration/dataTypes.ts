import { type Attribute, type DataTypeDefinition, type ImportedDataspecer, type Iri, ImportedObject, DataType, ImportedMorphism } from '@/types/integration';
import { Cardinality } from '@/types/schema';
import { createValueId, createMorphismId, createGeneratedId, CUSTOM_IRI_PREFIX } from './common';

const OFN_TYPE_PREFIX = "https://ofn.gov.cz/zdroj/základní-datové-typy/2020-07-01/";

const DEFAULT_DATA_TYPE = DataType.string;

export function parseDataType(iri: Iri | null | undefined): DataType {
    if (!iri)
        return DEFAULT_DATA_TYPE;

    const foundType = dataTypeDefinitions.find(definition => definition.iri === iri);

    if (!foundType)
        return DEFAULT_DATA_TYPE;

    return foundType.type;
}

export function createAttribute(attribute: Attribute, output: ImportedDataspecer): ImportedObject {
    const dataTypeDefinition = dataTypeDefinitions.find(definition => definition.type === attribute.dataType);

    if (!dataTypeDefinition)
        throw new Error('Data type not found');

    return dataTypeDefinition.createAttribute(attribute, output);
}


const dataTypeDefinitions: DataTypeDefinition[] = [
    {
        iri: OFN_TYPE_PREFIX + 'řetězec',
        type: DataType.string,
        createAttribute: createAttributeForString
    },
    {
        iri: OFN_TYPE_PREFIX + 'text',
        type: DataType.text,
        createAttribute: createAttributeForText
    }
];

function createAttributeForString(attribute: Attribute, output: ImportedDataspecer): ImportedObject {
    const newObject = new ImportedObject(attribute.iri, attribute.pimIri, attribute.label, createValueId());
    output.objects.push(newObject);

    return newObject;
}

const TEXT_IRI_PREFIX = CUSTOM_IRI_PREFIX + 'text/';
const TEXT = {
    ELEMENT: TEXT_IRI_PREFIX + 'element',
    ELEMENT_TO_ATTRIBUTE: TEXT_IRI_PREFIX + 'element-to-attribute',
    LANGUAGE: TEXT_IRI_PREFIX + 'language',
    ELEMENT_TO_LANGUAGE: TEXT_IRI_PREFIX + 'element-to-language',
    VALUE: TEXT_IRI_PREFIX + 'value',
    ELEMENT_TO_VALUE: TEXT_IRI_PREFIX + 'element-to-value'
};

function createAttributeForText(attribute: Attribute, output: ImportedDataspecer): ImportedObject {
    const attributeObject = new ImportedObject(attribute.iri, attribute.pimIri, attribute.label, createGeneratedId());
    output.objects.push(attributeObject);

    // TODO function createMap

    const element = new ImportedObject(attribute.iri + '/_language-element', TEXT.ELEMENT, '_element');
    output.objects.push(element);
    const elementToAttribute = new ImportedMorphism(attribute.iri + '/_element-to-attribute', TEXT.ELEMENT_TO_ATTRIBUTE, '', element, attributeObject, {
        domCodMin: Cardinality.One,
        domCodMax: Cardinality.One,
        codDomMin: Cardinality.Zero,
        codDomMax: Cardinality.Star
    });
    output.morphisms.push(elementToAttribute);

    const language = new ImportedObject(attribute.iri + '/_language', TEXT.LANGUAGE, '_language', createValueId());
    output.objects.push(language);
    const elementToLanguage = new ImportedMorphism(attribute.iri + '/_element-to-language', TEXT.ELEMENT_TO_LANGUAGE, '', element, language, {
        domCodMin: Cardinality.One,
        domCodMax: Cardinality.One,
        codDomMin: Cardinality.Zero,
        codDomMax: Cardinality.Star
    });
    output.morphisms.push(elementToLanguage);

    const value = new ImportedObject(attribute.iri + '/_value', TEXT.VALUE, '_value', createValueId());
    output.objects.push(value);
    const elementToValue = new ImportedMorphism(attribute.iri + '/_element-to-value', TEXT.ELEMENT_TO_VALUE, '', element, value, {
        domCodMin: Cardinality.One,
        domCodMax: Cardinality.One,
        codDomMin: Cardinality.Zero,
        codDomMax: Cardinality.Star
    });
    output.morphisms.push(elementToValue);

    element.addId(createMorphismId([ [ elementToLanguage ], [ elementToValue ], [ elementToAttribute ] ]));

    return attributeObject;
}
