import type { CardinalitySettings } from "@/types/schema";
import type { Iri } from "./parser";

export class ImportedObject {
    constructor(
        readonly iri: Iri,
        readonly label: string
    ) {}
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
