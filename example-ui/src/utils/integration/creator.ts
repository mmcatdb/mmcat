import { NodeSequence, type Node, Graph } from "@/types/categoryGraph";
import type { SchemaMorphism, SchemaObject } from "@/types/schema";
import type { ImportedMorphism, ImportedDataspecer, ImportedId, ImportedObject, MorphismSequence } from "@/types/integration";
import { Signature, Type } from "@/types/identifiers";
import type { Evocat } from "@/types/evocat/Evocat";

function sequenceToSignature(morphismSequence: MorphismSequence, node: Node, createdMorphisms: Map<ImportedMorphism, SchemaMorphism>): Signature | null {
    const nodeSequence = NodeSequence.fromRootNode(node, { selectNodes: false });

    for (const morphism of morphismSequence) {
        const schemaMorphism = createdMorphisms.get(morphism);
        if (!schemaMorphism) {
            console.log('Schema morphism for morphism not found.');
            return null;
        }

        const result = nodeSequence.addBaseSignature(schemaMorphism.signature);
        if (!result) {
            console.log('Base signature couldn\'t be added.');
            return null;
        }
    }

    return nodeSequence.toSignature();
}

function addId(id: ImportedId, evocat: Evocat, graph: Graph, object: SchemaObject, createdMorphisms: Map<ImportedMorphism, SchemaMorphism>) {
    if (id.type !== Type.Signatures) {
        evocat.createId(object, { type: id.type });
        return;
    }

    const node = graph.getNode(object);
    if (!node)
        return;

    const signatures = [];
    for (const morphismSequence of id.keys) {
        const signature = sequenceToSignature(morphismSequence, node, createdMorphisms);
        if (signature === null) {
            console.log('Id not valid for ' + object.label + '.', id, node);
            return;
        }
        signatures.push(signature);
    }

    evocat.createId(object, { signatures });
}

// TODO remove the graph dependency
export function addImportedToGraph(imported: ImportedDataspecer, evocat: Evocat, graph: Graph) {
    const iriToObjectMapping = new Map<string, SchemaObject>();
    const createdObjects = new Map() as Map<ImportedObject, SchemaObject>;
    const createdMorphisms = new Map() as Map<ImportedMorphism, SchemaMorphism>;

    imported.objects.forEach(object => {
        if (!evocat.schemaCategory.isIriAvailable(object.iri))
            return;

        const schemaObject = evocat.createObject({
            label: object.label,
            iri: object.iri,
            pimIri: object.pimIri,
        });

        iriToObjectMapping.set(object.iri, schemaObject);
        createdObjects.set(object, schemaObject);
    });

    imported.morphisms.forEach(morphism => {
        const object1 = iriToObjectMapping.get(morphism.dom.iri) || evocat.schemaCategory.findObjectByIri(morphism.dom.iri);
        if (!object1) {
            console.log('Dom object not found: ' + morphism.dom.iri);
            return;
        }

        const object2 = iriToObjectMapping.get(morphism.cod.iri) || evocat.schemaCategory.findObjectByIri(morphism.cod.iri);
        if (!object2) {
            console.log('Com object not found: ' + morphism.cod.iri);
            return;
        }

        if (!evocat.schemaCategory.isIriAvailable(morphism.iri))
            return;

        const schemaMorphism = evocat.createMorphism({
            dom: object1,
            cod: object2,
            min: morphism.min,
            iri: morphism.iri,
            pimIri: morphism.pimIri,
            label: morphism.label,
            tags: morphism.tags,
        });

        createdMorphisms.set(morphism, schemaMorphism);
    });

    // Only add ids on the newly created objects.
    createdObjects.forEach((schemaObject, object) => {
        object.ids.forEach(id => addId(id, evocat, graph, schemaObject, createdMorphisms));
    });

    graph.layout();
}
