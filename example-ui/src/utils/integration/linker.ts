import { ImportedMorphism, ImportedObject, type ImportedDataspecer, type Iri } from "@/types/integration";
import type { ParsedDataspecer } from "@/types/integration";
import { Cardinality, Tag, type CardinalitySettings } from "@/types/schema";
import { createAttribute } from "./dataTypes";
import { createValueId, createGeneratedId, CUSTOM_IRI_PREFIX } from "./common";

const CARDINALITY_ONE_TO_ONE: CardinalitySettings = {
    domCodMin: Cardinality.One,
    domCodMax: Cardinality.One,
    codDomMin: Cardinality.One,
    codDomMax: Cardinality.One
};

const CLASS_TO_ISA_IRI: Iri = CUSTOM_IRI_PREFIX + 'class-to-isa';

const REVERSED_SUFFIX = '/_reversed';

const ARRAY_IRI_PREFIX = CUSTOM_IRI_PREFIX + 'array/';
const ARRAY = {
    DOM_TO_ELEMENT_SUFFIX: '/_array',
    ELEMENT: ARRAY_IRI_PREFIX + 'element',
    ELEMENT_TO_COD: ARRAY_IRI_PREFIX + 'element-to-cod',
    INDEX: ARRAY_IRI_PREFIX + 'index',
    ELEMENT_TO_INDEX: ARRAY_IRI_PREFIX + 'element-to-index'
};

// Checks if the morhpism can be simple or if array is required instead.
function addMorphism(iri: Iri, pimIri: Iri, label: string, dom: ImportedObject, cod: ImportedObject, cardinalitySettings: CardinalitySettings, output: ImportedDataspecer) {
    // We can create simple morphism from dom to cod.
    if (cardinalitySettings.domCodMax === Cardinality.One) {
        const newMorphism = new ImportedMorphism(iri, pimIri, label, dom, cod, cardinalitySettings);
        output.morphisms.push(newMorphism);
        return newMorphism;
    }
    else if (cardinalitySettings.codDomMax === Cardinality.One) {
        const newLabel = label === '' ? '' : label + ' _reverse';

        const newMorphism = new ImportedMorphism(iri, pimIri + REVERSED_SUFFIX, newLabel, cod, dom, { // nosonar - This is OK, we are reversing the order of the morphism.
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
        const element = new ImportedObject(iri + '/_array-element', ARRAY.ELEMENT, '_array-element', createGeneratedId());
        output.objects.push(element);

        const index = new ImportedObject(iri + '/_index', ARRAY.INDEX, '_index', createValueId());
        output.objects.push(index);

        const elementToIndex = new ImportedMorphism(iri + '/_element-to-index', ARRAY.ELEMENT_TO_INDEX, '', element, index, CARDINALITY_ONE_TO_ONE);
        output.morphisms.push(elementToIndex);

        const elementToDomLabel = label === '' ? '' : label + ' _element-to-dom';
        const elementToDom = new ImportedMorphism(iri + '/_element-to-dom', pimIri + ARRAY.DOM_TO_ELEMENT_SUFFIX, elementToDomLabel, element, dom, {
            domCodMin: Cardinality.One,
            domCodMax: Cardinality.One,
            codDomMin: cardinalitySettings.domCodMin,
            codDomMax: cardinalitySettings.domCodMax
        }, [ Tag.Role ]);
        output.morphisms.push(elementToDom);

        const elementToCodLabel = label === '' ? '' : label + ' _element-to-cod';
        const elementToCod = new ImportedMorphism(iri + '/_element-to-cod', ARRAY.ELEMENT_TO_COD, elementToCodLabel, element, cod, {
            domCodMin: Cardinality.One,
            domCodMax: Cardinality.One,
            codDomMin: cardinalitySettings.codDomMin,
            codDomMax: cardinalitySettings.codDomMax
        }, [ Tag.Role ]);
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
        const object = new ImportedObject(myClass.iri, myClass.pimIri, myClass.label);
        object.addId(createValueId());
        output.objects.push(object);
    });

    classes.forEach(myClass => {
        if (myClass.extendsClassIris.length === 0)
            return;

        const object = output.objects.find(o => o.iri === myClass.iri);
        if (!object)
            return;

        myClass.extendsClassIris.forEach(ancestorIri => {
            const ancestorObject = output.objects.find(o => o.iri === ancestorIri);
            if (!ancestorObject)
                return;

            const isa = new ImportedMorphism(myClass.iri + '/_isa/' + ancestorIri, CLASS_TO_ISA_IRI, '', object, ancestorObject, CARDINALITY_ONE_TO_ONE, [ Tag.Isa ]);
            output.morphisms.push(isa);
        });
    });

    attributes.forEach(attribute => {
        const newObject = createAttribute(attribute, output);
        const parentObject = output.objects.find(object => object.iri === attribute.parentClassIri);

        if (!parentObject)
            return;

        addMorphism(attribute.iri + '/_morphism', attribute.pimIri, '', parentObject, newObject, {
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

        addMorphism(association.iri, association.pimIri, association.label, dom, cod, {
            domCodMin: codEnd.cardinality.min,
            domCodMax: codEnd.cardinality.max,
            codDomMin: domEnd.cardinality.min,
            codDomMax: domEnd.cardinality.max,
        }, output);
    });

    return output;
}
