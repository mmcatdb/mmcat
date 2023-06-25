import type { Iri } from '@/types/integration';
import { UniqueIdProvider } from '@/utils/UniqueIdProvider';
import { ComplexProperty, type ParentProperty } from '@/types/accessPath/basic';
import type { Entity, Id, VersionId } from '../id';
import { DynamicName, Key, Signature } from '../identifiers';
import type { LogicalModel } from '../logicalModel';
import { SchemaMorphism, type SchemaMorphismFromServer, type MorphismDefinition, VersionedSchemaMorphism } from './SchemaMorphism';
import { SchemaObject, type ObjectDefinition, type SchemaObjectFromServer, VersionedSchemaObject } from './SchemaObject';
import type { Graph } from '../categoryGraph';
import { ComparableMap } from '@/utils/ComparableMap';

export type SchemaCategoryFromServer = {
    id: Id;
    label: string;
    version: VersionId;
    objects: SchemaObjectFromServer[];
    morphisms: SchemaMorphismFromServer[];
};

export class SchemaCategory implements Entity {
    private notAvailableIris: Set<Iri> = new Set;

    private keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    private signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private constructor(
        readonly id: Id,
        readonly label: string,
        readonly versionId: VersionId,
        objects: SchemaObject[],
        morphisms: SchemaMorphism[],
        logicalModels: LogicalModel[],
    ) {
        objects.forEach(object => {
            if (object.iri)
                this.notAvailableIris.add(object.iri);

            const versionedObject = this.getObject(object.key);
            versionedObject.current = object;
        });

        morphisms.forEach(morphism => {
            if (morphism.iri)
                this.notAvailableIris.add(morphism.iri);

            const versionedMorphism = this.getMorphism(morphism.signature);
            versionedMorphism.current = morphism;
        });

        logicalModels.forEach(logicalModel => {
            logicalModel.mappings.forEach(mapping => {
                const pathObjects = getObjectsFromPath(mapping.accessPath, objects, morphisms);

                const rootObject = objects.find(object => object.key.equals(mapping.rootObjectKey));
                if (rootObject)
                    pathObjects.push(rootObject);

                pathObjects.forEach(object => this.getObject(object.key)?.addLogicalModel(logicalModel));
            });
        });
    }

    static fromServer(input: SchemaCategoryFromServer, logicalModels: LogicalModel[]): SchemaCategory {
        const morphisms = input.morphisms.map(SchemaMorphism.fromServer);

        return new SchemaCategory(
            input.id,
            input.label,
            input.version,
            input.objects.map(SchemaObject.fromServer),
            morphisms,
            logicalModels,
        );
    }

    private objects = new ComparableMap<Key, number, VersionedSchemaObject>(key => key.value);
    private morphisms = new ComparableMap<Signature, string, VersionedSchemaMorphism>(signature => signature.value);

    createObject(def: ObjectDefinition): VersionedSchemaObject {
        if ('iri' in def)
            this.notAvailableIris.add(def.iri);

        const key = this.keysProvider.createAndAdd();
        return this.getObject(key);
    }

    getObject(key: Key): VersionedSchemaObject {
        let object = this.objects.get(key);

        if (!object) {
            object = VersionedSchemaObject.create(key, this._graph);
            this.objects.set(key, object);
            this.keysProvider.add(key);
        }

        return object;
    }

    createMorphism(def: MorphismDefinition): VersionedSchemaMorphism {
        if ('iri' in def)
            this.notAvailableIris.add(def.iri);

        const signature = this.signatureProvider.createAndAdd();
        return this.getMorphism(signature);
    }

    getMorphism(signature: Signature): VersionedSchemaMorphism {
        let morphism = this.morphisms.get(signature);

        if (!morphism) {
            morphism = VersionedSchemaMorphism.create(signature, this._graph);
            this.morphisms.set(signature, morphism);
            this.signatureProvider.add(signature);
        }

        return morphism;
    }

    findObjectByIri(iri: Iri): SchemaObject | undefined {
        return [ ...this.objects.values() ].map(object => object.current).find(object => object?.iri === iri);
    }

    isIriAvailable(iri: Iri): boolean {
        return !this.notAvailableIris.has(iri);
    }

    private _graph?: Graph;

    get graph(): Graph | undefined {
        return this._graph;
    }

    set graph(newGraph: Graph | undefined) {
        this._graph = newGraph;
        if (!newGraph) {
            this.objects.forEach(object => object.graph = undefined);
            this.morphisms.forEach(morphism => morphism.graph = undefined);
            return;
        }

        newGraph.resetElements();
        newGraph.batch(() => {
            this.objects.forEach(object => object.graph = newGraph);
            this.morphisms.forEach(morphism => morphism.graph = newGraph);
        });

        // Position the object to the center of the canvas.
        newGraph.fixLayout();
        newGraph.layout();
        newGraph.center();
    }
}

function getObjectsFromPath(path: ParentProperty, objects: SchemaObject[], morphisms: SchemaMorphism[]): SchemaObject[] {
    const output: SchemaObject[] = [];

    path.subpaths.forEach(subpath => {
        const subpathObject = findObjectFromSignature(subpath.signature, objects, morphisms);
        if (subpathObject)
            output.push(subpathObject);

        if (subpath.name instanceof DynamicName) {
            const nameObject = findObjectFromSignature(subpath.name.signature, objects, morphisms);
            if (nameObject)
                output.push(nameObject);
        }

        if (subpath instanceof ComplexProperty)
            output.push(...getObjectsFromPath(subpath, objects, morphisms));
    });

    return output;
}

function findObjectFromSignature(signature: Signature, objects: SchemaObject[], morphisms: SchemaMorphism[]): SchemaObject | undefined {
    const base = signature.getLastBase();
    if (!base)
        return undefined;

    const morphism = morphisms.find(morphism => morphism.signature.equals(base.last));
    if (!morphism)
        return undefined;

    return objects.find(object => object.key.equals(morphism.codKey));
}

export type SchemaCategoryInfoFromServer = {
    id: Id;
    label: string;
    version: VersionId;
};

export class SchemaCategoryInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly versionId: VersionId,
    ) {}

    static fromServer(input: SchemaCategoryInfoFromServer): SchemaCategoryInfo {
        return new SchemaCategoryInfo(
            input.id,
            input.label,
            input.version,
        );
    }
}

export type SchemaCategoryInit = {
    label: string;
};
