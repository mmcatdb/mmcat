import { NodeSequence, type Edge, type Graph, type Node } from "@/types/categoryGraph";
import type { SchemaObject } from "@/types/schema";
import type { ImportedMorphism, ImportedDataspecer, ImportedId, ImportedObject, MorphismSequence } from "@/types/integration";
import { SignatureIdFactory, Signature, Type } from "@/types/identifiers";

function sequenceToSignature(morphismSequence: MorphismSequence, node: Node, createdMorphisms: Map<ImportedMorphism, Edge>): Signature | null {
    const nodeSequence = NodeSequence.fromRootNode(node, { selectNodes: false });

    for (const morphism of morphismSequence) {
        const edge = createdMorphisms.get(morphism);
        if (!edge) {
            console.log('Edge for morphism not found.');
            return null;
        }

        const result = nodeSequence.addBaseSignature(edge.schemaMorphism.signature);
        if (!result) {
            console.log('Base signature couldn\'t be added.');
            return null;
        }
    }

    return nodeSequence.toSignature();
}

function addId(id: ImportedId, node: Node, createdMorphisms: Map<ImportedMorphism, Edge>) {
    if (id.type !== Type.Signatures) {
        node.addNonSignatureId(id.type);
        return;
    }

    const factory = new SignatureIdFactory();
    for (const morphismSequence of id.keys) {
        const signature = sequenceToSignature(morphismSequence, node, createdMorphisms);
        if (signature === null) {
            console.log('Id not valid for ' + node.schemaObject.label + '.', id, node);
            return;
        }
        factory.addSignature(signature);
    }

    node.addSignatureId(factory.signatureId);
}

export function addImportedToGraph(imported: ImportedDataspecer, graph: Graph) {
    const iriToObjectMapping = new Map<string, SchemaObject>();
    const createdObjects = new Map() as Map<ImportedObject, Node>;
    const createdMorphisms = new Map() as Map<ImportedMorphism, Edge>;

    imported.objects.forEach(object => {
        const schemaObject = graph.schemaCategory.createObjectWithIri(object.label, undefined, object.iri);
        if (!schemaObject)
            return;

        iriToObjectMapping.set(object.iri, schemaObject);
        const node = graph.createNode(schemaObject, 'new');
        createdObjects.set(object, node);
    });

    imported.morphisms.forEach(morphism => {
        const object1 = iriToObjectMapping.get(morphism.dom.iri) || graph.schemaCategory.findObjectByIri(morphism.dom.iri);
        if (!object1) {
            console.log('Dom object not found: ' + morphism.dom.iri);
            return;
        }

        const object2 = iriToObjectMapping.get(morphism.cod.iri) || graph.schemaCategory.findObjectByIri(morphism.cod.iri);
        if (!object2) {
            console.log('Com object not found: ' + morphism.cod.iri);
            return;
        }

        const schemaMorphism = graph.schemaCategory.createMorphismWithDualWithIri(object1, object2, morphism.cardinalitySettings, morphism.iri, morphism.label, morphism.tags);
        if (!schemaMorphism)
            return;

        const edges = graph.createEdgeWithDual(schemaMorphism, 'new');
        createdMorphisms.set(morphism, edges[0]);
    });

    // Only add ids on the newly created objects.
    createdObjects.forEach((node, object) => {
        object.ids.forEach(id => addId(id, node, createdMorphisms));
    });

    graph.layout();
}
