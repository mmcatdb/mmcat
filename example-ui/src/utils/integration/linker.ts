import { Cardinality, type CardinalitySettings } from "@/types/schema";
import type { Iri, ParsedDataspecer } from "./parser";

class ImportedObject {
    constructor(
        readonly iri: Iri,
        readonly label: string
    ) {}
}

class ImportedMorphism {
    constructor(
        readonly iri: Iri,
        readonly dom: ImportedObject,
        readonly cod: ImportedObject,
        readonly cardinalitySettings: CardinalitySettings
    ) {}
}

export type ImportedDataspecer = {
    objects: ImportedObject[];
    morphisms: ImportedMorphism[];
}

export function linkDataspecer(input: ParsedDataspecer): ImportedDataspecer {
    const { classes, attributes, associations, associationEnds } = input;

    const output: ImportedDataspecer = {
        objects: [],
        morphisms: []
    };

    classes.forEach(myClass => {
        const newObject = new ImportedObject(myClass.iri, myClass.label);
        output.objects.push(newObject);
    });

    attributes.forEach(attribute => {
        const newObject = new ImportedObject(attribute.iri, attribute.label);
        const parentObject = output.objects.find(object => object.iri === attribute.parentClassIri);

        output.objects.push(newObject);

        if (!parentObject)
            return;

        const newMorphism = new ImportedMorphism(attribute.iri, parentObject, newObject, {
            domCodMin: attribute.cardinality.min,
            domCodMax: attribute.cardinality.max,
            codDomMin: Cardinality.Zero,
            codDomMax: Cardinality.Star
        });

        output.morphisms.push(newMorphism);
    });

    associations.forEach(association => {
        const domEnd = associationEnds.find(end => end.iri === association.domEndIri);
        const codEnd = associationEnds.find(end => end.iri === association.codEndIri);

        if (!domEnd || !codEnd)
            return;

        const dom = output.objects.find(object => object.iri === domEnd.classIri);
        const cod = output.objects.find(object => object.iri === codEnd.classIri);

        if (!dom || !cod)
            return;

        const newMorphism = new ImportedMorphism(association.iri, dom, cod, {
            domCodMin: codEnd.cardinality.min,
            domCodMax: codEnd.cardinality.max,
            codDomMin: domEnd.cardinality.min,
            codDomMax: domEnd.cardinality.max,
        });

        output.morphisms.push(newMorphism);
    });

    return output;
}
