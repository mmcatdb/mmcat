import { ImportedMorphism, ImportedObject, type ImportedDataspecer } from "@/types/integration";
import type { ParsedDataspecer } from "@/types/integration";
import { Cardinality, type CardinalitySettings } from "@/types/schema";
import { createAttribute } from "./dataTypes";

// Checks if the morhpism can be simple or if array is required instead.
function addMorphism(iri: string, label: string, dom: ImportedObject, cod: ImportedObject, cardinalitySettings: CardinalitySettings, output: ImportedDataspecer) {
    // We can create simple morphism from dom to cod.
    if (cardinalitySettings.domCodMax === Cardinality.One) {
        const newMorphism = new ImportedMorphism(iri, label, dom, cod, cardinalitySettings);
        output.morphisms.push(newMorphism);
        return newMorphism;
    }
    else if (cardinalitySettings.codDomMax === Cardinality.One) {
        const newLabel = label === '' ? '' : label + ' _reverse';
        const newMorphism = new ImportedMorphism(iri, newLabel, cod, dom, {
            domCodMin: cardinalitySettings.codDomMin,
            domCodMax: cardinalitySettings.codDomMax,
            codDomMin: cardinalitySettings.domCodMin,
            codDomMax: cardinalitySettings.domCodMax
        });
        output.morphisms.push(newMorphism);
        return newMorphism;
    }
    // Nope, array is required.
    else {
        const element = new ImportedObject(iri + '/_array-element', '_array-element');
        output.objects.push(element);

        const elementToDomLabel = label === '' ? '' : label + ' _element-to-dom';
        const elementToDom = new ImportedMorphism(iri + '/_element-to-dom', elementToDomLabel, element, dom, {
            domCodMin: Cardinality.One,
            domCodMax: Cardinality.One,
            codDomMin: cardinalitySettings.domCodMin,
            codDomMax: cardinalitySettings.domCodMax
        });
        output.morphisms.push(elementToDom);

        const elementToCodLabel = label === '' ? '' : label + ' _element-to-cod';
        const elementToCod = new ImportedMorphism(iri + '/_element-to-cod', elementToCodLabel, element, cod, {
            domCodMin: Cardinality.One,
            domCodMax: Cardinality.One,
            codDomMin: cardinalitySettings.codDomMin,
            codDomMax: cardinalitySettings.codDomMax
        });
        output.morphisms.push(elementToCod);
    }
}


export function linkDataspecer(input: ParsedDataspecer): ImportedDataspecer {
    const { classes, attributes, associations, associationEnds } = input;

    const output: ImportedDataspecer = {
        objects: [],
        morphisms: [],
        counts: {
            classes: classes.length,
            attributes: attributes.length,
            associations: associations.length,
            associationEnds: associationEnds.length
        }
    };

    classes.forEach(myClass => {
        const newObject = new ImportedObject(myClass.iri, myClass.label);
        output.objects.push(newObject);
    });

    attributes.forEach(attribute => {
        const newObject = createAttribute(attribute, output);
        const parentObject = output.objects.find(object => object.iri === attribute.parentClassIri);

        if (!parentObject)
            return;

        addMorphism(attribute.iri + '/_morphism', '', parentObject, newObject, {
            domCodMin: attribute.cardinality.min,
            domCodMax: attribute.cardinality.max,
            codDomMin: Cardinality.Zero,
            codDomMax: Cardinality.Star
        }, output);
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

        addMorphism(association.iri, association.label, dom, cod, {
            domCodMin: codEnd.cardinality.min,
            domCodMax: codEnd.cardinality.max,
            codDomMin: domEnd.cardinality.min,
            codDomMax: domEnd.cardinality.max,
        }, output);
    });

    return output;
}
