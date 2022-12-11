import type { Max, Min } from "@/types/schema";
import type { DataType } from "./dataTypes";

export type Iri = string;

export type DataspecerEntity = {
    iri: Iri;
}

export type Class = DataspecerEntity & {
    label: string;
    extendsClassIris: Iri[];
};

export type Attribute = DataspecerEntity & {
    label: string;
    parentClassIri: Iri;
    dataType: DataType;
    cardinality: {
        min: Min;
        max: Max;
    };
};

export type Association = DataspecerEntity & {
    label: string;
    domEndIri: Iri;
    codEndIri: Iri;
};

export type AssociationEnd = DataspecerEntity & {
    classIri: Iri,
    cardinality: {
        min: Min;
        max: Max;
    };
};

export type ParsedDataspecer = {
    classes: Class[];
    attributes: Attribute[];
    associations: Association[];
    associationEnds: AssociationEnd[];
}
