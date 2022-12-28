import type { CardinalitySettings, Tag } from "@/types/schema";
import type { Type } from "../identifiers";
import type { Iri } from "./parser";

export type MorphismSequence = ImportedMorphism[];

export type ImportedId = {
    type: Type.Value;
} | {
    type: Type.Generated;
} | {
    type: Type.Signatures;
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
        readonly cardinalitySettings: CardinalitySettings,
        readonly tags: Tag[] = []
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
