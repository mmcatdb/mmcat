import type { Graph } from "@/types/categoryGraph";
import type { SchemaObject } from "@/types/schema";
import type { ImportedDataspecer } from "./linker";

export function addImportedToGraph(imported: ImportedDataspecer, graph: Graph) {
    const iriToObjectMapping = new Map<string, SchemaObject>();

    imported.objects.forEach(importedObject => {
        const object = graph.schemaCategory.createObjectWithIri(importedObject.label, [], importedObject.iri);
        if (!object)
            return;

        iriToObjectMapping.set(importedObject.iri, object);
        graph.createNode(object, 'new');
    });

    imported.morphisms.forEach(importedMorphism => {
        const object1 = iriToObjectMapping.get(importedMorphism.dom.iri) || graph.schemaCategory.findObjectByIri(importedMorphism.dom.iri);
        if (!object1) {
            console.log('Dom object not found: ' + importedMorphism.dom.iri);
            return;
        }

        const object2 = iriToObjectMapping.get(importedMorphism.cod.iri) || graph.schemaCategory.findObjectByIri(importedMorphism.cod.iri);
        if (!object2) {
            console.log('Com object not found: ' + importedMorphism.cod.iri);
            return;
        }

        const morphism = graph.schemaCategory.createMorphismWithDualWithIri(object1, object2, importedMorphism.cardinalitySettings, importedMorphism.iri);
        if (!morphism)
            return;

        graph.createEdgeWithDual(morphism, 'new');
    });
}
