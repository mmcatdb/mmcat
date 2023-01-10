import type { ImportedDataspecer, ImportedObject } from "./linker";
import type { Attribute } from "./parser";

export enum DataType {
    string = 'string',
    text = 'text'
}

type AttributeCreatorFunction = (attribute: Attribute, output: ImportedDataspecer) => ImportedObject;

export type DataTypeDefinition = {
    iri: string;
    type: DataType;
    createAttribute: AttributeCreatorFunction;
};
