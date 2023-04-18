import { type Attribute, type DataTypeDefinition, type ImportedDataspecer, type Iri, ImportedObject, DataType, ImportedMorphism } from '@/types/integration';
import { Cardinality } from '@/types/schema';
import { createValueId, createMorphismId, CUSTOM_IRI_PREFIX } from './common';

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
        createAttribute: createAttributeForString,
    },
    {
        iri: OFN_TYPE_PREFIX + 'text',
        type: DataType.text,
        createAttribute: createAttributeForText,
    },
];

function createAttributeForString(attribute: Attribute, output: ImportedDataspecer): ImportedObject {
    const newObject = new ImportedObject(attribute.iri, attribute.pimIri, attribute.label, createValueId());
    output.objects.push(newObject);

    return newObject;
}

const TEXT_IRI_PREFIX = CUSTOM_IRI_PREFIX + 'text/';
const TEXT = {
    LANGUAGE: TEXT_IRI_PREFIX + 'language',
    ATTRIBUTE_TO_LANGUAGE: TEXT_IRI_PREFIX + 'attribute-to-language',
    VALUE: TEXT_IRI_PREFIX + 'value',
    ATTRIBUTE_TO_VALUE: TEXT_IRI_PREFIX + 'attribute-to-value',
};

function createAttributeForText(attribute: Attribute, output: ImportedDataspecer): ImportedObject {
    const attributeObject = new ImportedObject(attribute.iri, attribute.pimIri, attribute.label);
    output.objects.push(attributeObject);

    const language = new ImportedObject(attribute.iri + '/_language', TEXT.LANGUAGE, attribute.label + '_language', createValueId());
    output.objects.push(language);
    const attributeToLanguage = new ImportedMorphism(attribute.iri + '/_attribute-to-language', TEXT.ATTRIBUTE_TO_LANGUAGE, '', attributeObject, language, Cardinality.One);
    output.morphisms.push(attributeToLanguage);

    const value = new ImportedObject(attribute.iri + '/_value', TEXT.VALUE, attribute.label + '_value', createValueId());
    output.objects.push(value);
    const attributeToValue = new ImportedMorphism(attribute.iri + '/_attribute-to-value', TEXT.ATTRIBUTE_TO_VALUE, '', attributeObject, value, Cardinality.One);
    output.morphisms.push(attributeToValue);

    attributeObject.addId(createMorphismId([ [ attributeToLanguage ], [ attributeToValue ] ]));

    return attributeObject;
}
