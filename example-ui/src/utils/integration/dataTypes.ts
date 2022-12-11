import type { Attribute, DataTypeDefinition, ImportedDataspecer, Iri } from '@/types/integration';
import { ImportedMorphism } from '@/types/integration';
import { DataType, ImportedObject } from '@/types/integration';
import { Cardinality } from '@/types/schema';

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
    const newObject = new ImportedObject(attribute.iri, attribute.label);
    output.objects.push(newObject);

    return newObject;
}

function createAttributeForText(attribute: Attribute, output: ImportedDataspecer): ImportedObject {
    const attributeObject = new ImportedObject(attribute.iri, attribute.label);
    output.objects.push(attributeObject);

    // TODO function createMap

    const elementObject = new ImportedObject(attribute.iri + '/_element', '_element');
    output.objects.push(elementObject);
    const attributeToElementMorphism = new ImportedMorphism(attribute.iri + '/_attribute-to-element', '', attributeObject, elementObject, {
        domCodMin: Cardinality.Zero,
        domCodMax: Cardinality.Star,
        codDomMin: Cardinality.One,
        codDomMax: Cardinality.One
    });
    output.morphisms.push(attributeToElementMorphism);

    const languageObject = new ImportedObject(attribute.iri + '/_language', '_language');
    output.objects.push(languageObject);
    const elementToLanguageMorphism = new ImportedMorphism(attribute.iri + '/_element-to-language', '', elementObject, languageObject, {
        domCodMin: Cardinality.One,
        domCodMax: Cardinality.One,
        codDomMin: Cardinality.Zero,
        codDomMax: Cardinality.Star
    });
    output.morphisms.push(elementToLanguageMorphism);

    const valueObject = new ImportedObject(attribute.iri + '/_value', '_value');
    output.objects.push(valueObject);
    const elementToValueMorphism = new ImportedMorphism(attribute.iri + '/_element-to-value', '', elementObject, valueObject, {
        domCodMin: Cardinality.One,
        domCodMax: Cardinality.One,
        codDomMin: Cardinality.Zero,
        codDomMax: Cardinality.Star
    });
    output.morphisms.push(elementToValueMorphism);

    return attributeObject;
}
