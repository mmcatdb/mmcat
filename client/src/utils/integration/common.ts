import { Type } from '@/types/identifiers';
import type { ImportedMorphism, ImportedId } from '@/types/integration';

export const CUSTOM_IRI_PREFIX = import.meta.env.VITE_CUSTOM_IRI_PREFIX;

export function createValueId(): ImportedId {
    return { type: Type.Value };
}

export function createGeneratedId(): ImportedId {
    return { type: Type.Generated };
}

export function createMorphismId(morphisms: ImportedMorphism | ImportedMorphism[][]): ImportedId {
    return {
        type: Type.Signatures,
        keys: Array.isArray(morphisms) ? morphisms : [ [ morphisms ] ],
    };
}

