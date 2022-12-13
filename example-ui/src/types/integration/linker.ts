import type { CardinalitySettings } from "@/types/schema";
import type { Iri } from "./parser";

export type MorphismSequence = ImportedMorphism[];

export enum IdType {
    Empty,
    Technical,
    Morphism
}

export type ImportedId = {
    type: IdType.Empty;
} | {
    type: IdType.Technical;
} | {
    type: IdType.Morphism;
    keys: MorphismSequence[];
}

export class ImportedObject {
    ids: ImportedId[];

    constructor(
        readonly iri: Iri,
        readonly label: string,
        ids: ImportedId | ImportedId[] = []
    ) {
        this.ids = Array.isArray(ids) ? ids : [ ids ];
    }

    addId(id: ImportedId) {
        this.ids.push(id);
    }
}

export class ImportedMorphism {
    constructor(
        readonly iri: Iri,
        readonly label: string,
        readonly dom: ImportedObject,
        readonly cod: ImportedObject,
        readonly cardinalitySettings: CardinalitySettings
    ) {}
}

export type ImportedDataspecer = {
    objects: ImportedObject[];
    morphisms: ImportedMorphism[];
    counts: {
        classes: number;
        attributes: number;
        associations: number;
        associationEnds: number;
    };
}
