import { NodeSequence, type Node, Graph } from '@/types/categoryGraph';
import type { SchemaObject, SchemaMorphism } from '@/types/schema';
import type { ImportedMorphism, ImportedDataspecer, ImportedObject, MorphismSequence, ImportedSignatureId } from '@/types/integration';
import { ObjectIds, Signature, SignatureId, Type } from '@/types/identifiers';
import type { Evocat } from '@/types/evocat/Evocat';

export function addImportedToGraph(imported: ImportedDataspecer, evocat: Evocat, graph: Graph) {
    evocat.compositeOperation('integration', () => {
        innerAddImportedToGraph(imported, evocat, graph);
    });
}

// TODO remove the graph dependency
function innerAddImportedToGraph(imported: ImportedDataspecer, evocat: Evocat, graph: Graph) {
    const iriToObjectMapping = new Map<string, SchemaObject>();
    const createdObjects: Map<ImportedObject, SchemaObject> = new Map();
    const createdMorphisms: Map<ImportedMorphism, SchemaMorphism> = new Map();

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
        const ids = createObjectIds(object, schemaObject, graph, createdMorphisms);
        if (!ids)
            return;

        const update = {
            ...schemaObject.toDefinition(),
            ids,
        };
        evocat.editObject(update, schemaObject);
    });

    graph.layout();
}

function createObjectIds(object: ImportedObject, schemaObject: SchemaObject, graph: Graph, createdMorphisms: Map<ImportedMorphism, SchemaMorphism>): ObjectIds | undefined {
    if (object.ids.length === 0)
        return undefined;

    if (object.ids.length === 1) {
        const firstId = object.ids[0];
        if (firstId.type !== Type.Signatures)
            return ObjectIds.createNonSignatures(firstId.type);
    }

    const importedSignatureIds = object.ids.filter((id): id is ImportedSignatureId => id.type === Type.Signatures);
    if (importedSignatureIds.length !== object.ids.length) {
        console.log(`Some non-signature ids detected for object ${object.label}.`);
        return undefined;
    }

    const node = graph.getNode(schemaObject);
    if (!node) {
        console.log(`Node not found for object ${schemaObject.label}`);
        return undefined;
    }
    const signatureIds = importedSignatureIds
        .map(id => createSignatureId(id.keys, node, createdMorphisms))
        .filter((signatureId): signatureId is SignatureId => !!signatureId);

    return ObjectIds.createSignatures(signatureIds);
}

function createSignatureId(keys: MorphismSequence[], node: Node, createdMorphisms: Map<ImportedMorphism, SchemaMorphism>): SignatureId | undefined {
    const signatures = [];
    for (const morphismSequence of keys) {
        const signature = sequenceToSignature(morphismSequence, node, createdMorphisms);
        if (signature === null) {
            console.log(`Id not valid for object ${node.schemaObject.label}.`, node);
            return;
        }
        signatures.push(signature);
    }

    return new SignatureId(signatures);
}

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
