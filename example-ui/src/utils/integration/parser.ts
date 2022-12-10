import { Cardinality, type Max, type Min } from "@/types/schema";

export type Iri = string;

type DataspecerEntity = {
    iri: Iri;
}

function getLanguageString(object: { cs?: string, en?: string } | null): string {
    return object?.cs || object?.en || '';
}

type Class = DataspecerEntity & {
    label: string;
    extendsClassIris: Iri[];
};

function parseClass(resource: any): Class {
    return {
        iri: resource.iri,
        label: getLanguageString(resource.pimHumanLabel),
        extendsClassIris: resource.pimExtends
    };
}

type Attribute = DataspecerEntity & {
    label: string;
    parentClassIri: Iri;
    cardinality: {
        min: Min;
        max: Max;
    };
};

function parseCardinality(resource: { pimCardinalityMin: 1 | unknown, pimCardinalityMax: 1 | unknown }): { min: Min, max: Max } {
    return {
        min: resource.pimCardinalityMin === 1 ? Cardinality.One : Cardinality.Zero,
        max: resource.pimCardinalityMax === 1 ? Cardinality.One : Cardinality.Star
    };
}

function parseAttribute(resource: any): Attribute {
    return {
        iri: resource.iri,
        label: getLanguageString(resource.pimHumanLabel),
        parentClassIri: resource.pimOwnerClass,
        cardinality: parseCardinality(resource)
    };
}

type Association = DataspecerEntity & {
    domEndIri: Iri;
    codEndIri: Iri;
};

function parseAssociation(resource: any): Association {
    return {
        iri: resource.iri,
        domEndIri: resource.pimEnd[0],
        codEndIri: resource.pimEnd[1]
    };
}

type AssociationEnd = DataspecerEntity & {
    classIri: Iri,
    cardinality: {
        min: Min;
        max: Max;
    };
};

function parseAssociationEnd(resource: any): AssociationEnd {
    return {
        iri: resource.iri,
        classIri: resource.pimPart,
        cardinality: parseCardinality(resource)
    };
}

export type ParsedDataspecer = {
    classes: Class[];
    attributes: Attribute[];
    associations: Association[];
    associationEnds: AssociationEnd[];
}

export function parseDataspecer({ resources }: any): ParsedDataspecer {
    const output: ParsedDataspecer = {
        classes: [],
        attributes: [],
        associations: [],
        associationEnds: []
    };

    for (const key in resources) {
        const resource = resources[key];

        if (key.includes('/class/'))
            output.classes.push(parseClass(resource));
        else if (key.includes('/attribute/'))
            output.attributes.push(parseAttribute(resource));
        else if (key.includes('/association/'))
            output.associations.push(parseAssociation(resource));
        else if (key.includes('/association-end/'))
            output.associationEnds.push(parseAssociationEnd(resource));
    }

    return output;
}
