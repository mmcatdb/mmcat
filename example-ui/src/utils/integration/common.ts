import { type ImportedMorphism, type ImportedId, IdType } from "@/types/integration";

export function createEmptyId(): ImportedId {
    return { type: IdType.Empty };
}

export function createTechnicalId(): ImportedId {
    return { type: IdType.Technical };
}

export function createMorphismId(morphisms: ImportedMorphism | ImportedMorphism[]): ImportedId {
    return {
        type: IdType.Morphism,
        keys: [ Array.isArray(morphisms) ? morphisms : [ morphisms ] ]
    };
}
