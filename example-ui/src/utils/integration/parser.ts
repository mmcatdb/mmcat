import type { Class, Attribute, Association, AssociationEnd, ParsedDataspecer } from "@/types/integration";
import { Cardinality, type Max, type Min } from "@/types/schema";
import { parseDataType } from "./dataTypes";

function getLanguageString(object: { cs?: string, en?: string } | null): string {
    return object?.cs || object?.en || '';
}

function parseClass(resource: any): Class {
    return {
        iri: resource.iri,
        pimIri: resource.pimInterpretation,
        label: getLanguageString(resource.pimHumanLabel),
        extendsClassIris: resource.pimExtends
    };
}

function parseCardinality(resource: { pimCardinalityMin: 1 | unknown, pimCardinalityMax: 1 | unknown }): { min: Min, max: Max } {
    return {
        min: resource.pimCardinalityMin === 1 ? Cardinality.One : Cardinality.Zero,
        max: resource.pimCardinalityMax === 1 ? Cardinality.One : Cardinality.Star
    };
}

function parseAttribute(resource: any): Attribute {
    return {
        iri: resource.iri,
        pimIri: resource.pimInterpretation,
        label: getLanguageString(resource.pimHumanLabel),
        dataType: parseDataType(resource.pimDatatype),
        parentClassIri: resource.pimOwnerClass,
        cardinality: parseCardinality(resource)
    };
}

function parseAssociation(resource: any): Association {
    return {
        iri: resource.iri,
        pimIri: resource.pimInterpretation,
        label: getLanguageString(resource.pimHumanLabel),
        domEndIri: resource.pimEnd[0],
        codEndIri: resource.pimEnd[1]
    };
}

function parseAssociationEnd(resource: any): AssociationEnd {
    return {
        iri: resource.iri,
        classIri: resource.pimPart,
        cardinality: parseCardinality(resource)
    };
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
