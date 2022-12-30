import { Type } from "@/types/identifiers";
import type { ImportedMorphism, ImportedId } from "@/types/integration";

export function createValueId(): ImportedId {
    return { type: Type.Value };
}

export function createGeneratedId(): ImportedId {
    return { type: Type.Generated };
}

export function createMorphismId(morphisms: ImportedMorphism | ImportedMorphism[][]): ImportedId {
    return {
        type: Type.Signatures,
        keys: Array.isArray(morphisms) ? morphisms : [ [ morphisms ] ]
    };
}

export const CUSTOM_IRI_PREFIX = 'https://mm-evocat.com/iri/';
