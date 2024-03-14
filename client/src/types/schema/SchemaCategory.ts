import { UniqueIdProvider } from '@/utils/UniqueIdProvider';
import { ComplexProperty, type ParentProperty } from '@/types/accessPath/basic';
import type { Entity, Id, VersionId } from '../id';
import { DynamicName, Key, Signature } from '../identifiers';
import type { LogicalModel } from '../logicalModel';
import { SchemaMorphism, type SchemaMorphismFromServer, VersionedSchemaMorphism } from './SchemaMorphism';
import { SchemaObject, type SchemaObjectFromServer, VersionedSchemaObject } from './SchemaObject';
import type { Graph } from '../categoryGraph';
import { ComparableMap } from '@/utils/ComparableMap';
import type { Mapping } from '../mapping';
import type { Type } from '../database';
import { ComparableSet } from '@/utils/ComparableSet';

export type SchemaCategoryFromServer = {
    id: Id;
    label: string;
    version: VersionId;
    objects: SchemaObjectFromServer[];
    morphisms: SchemaMorphismFromServer[];
};

export class SchemaCategory implements Entity {
    private keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    private signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private readonly groups: GroupData[];

    private constructor(
        readonly id: Id,
        readonly label: string,
        readonly versionId: VersionId,
        objects: VersionedSchemaObject[],
        morphisms: SchemaMorphism[],
        logicalModels: LogicalModel[],
    ) {
        objects.forEach(object => {
            if (!object.current)
                return;

            this.objects.set(object.key, object);
            this.keysProvider.add(object.key);
        });

        morphisms.forEach(morphism => {
            const versionedMorphism = this.getMorphism(morphism.signature);
            versionedMorphism.current = morphism;
        });

        this.groups = createGroups(logicalModels, objects, morphisms);
        this.groups.forEach(group => {
            group.mappings.forEach(mapping => {
                mapping.properties.forEach(property => {
                    property.addGroup(group.id);
                });
                mapping.root.addGroup(group.id);
            });
        });
    }

    static fromServer(input: SchemaCategoryFromServer, logicalModels: LogicalModel[]): SchemaCategory {
        const morphisms = input.morphisms.map(SchemaMorphism.fromServer);

        return new SchemaCategory(
            input.id,
            input.label,
            input.version,
            input.objects.map(VersionedSchemaObject.fromServer),
            morphisms,
            logicalModels,
        );
    }

    private objects = new ComparableMap<Key, number, VersionedSchemaObject>(key => key.value);
    private morphisms = new ComparableMap<Signature, string, VersionedSchemaMorphism>(signature => signature.value);

    createObject(): VersionedSchemaObject {
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

    getObjects(): VersionedSchemaObject[] {
        return [ ...this.objects.values() ];
    }

    createMorphism(): VersionedSchemaMorphism {
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

        newGraph.resetElements(this.groups);
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

export type GroupMapping = {
    mapping: Mapping;
    properties: VersionedSchemaObject[];
    root: VersionedSchemaObject;
    groupId: string;
};

export type GroupData = {
    id: string;
    logicalModel: LogicalModel;
    mappings: GroupMapping[];
};

type Context = {
    objects: ComparableMap<Key, number, SchemaObject>;
    morphisms: ComparableMap<Signature, string, SchemaMorphism>;
};

function createGroups(logicalModels: LogicalModel[], objects: VersionedSchemaObject[], morphisms: SchemaMorphism[]): GroupData[] {
    const context: Context = {
        objects: new ComparableMap(key => key.value),
        morphisms: new ComparableMap(signature => signature.value),
    };

    objects
        .map(object => object.current)
        .filter((o): o is SchemaObject => !!o)
        .forEach(object => context.objects.set(object.key, object));

    morphisms.forEach(morphism => context.morphisms.set(morphism.signature, morphism));

    const typeIndices = new Map<Type, number>();

    return logicalModels.map(logicalModel => {
        const nextIndex = typeIndices.get(logicalModel.database.type) ?? 0;
        typeIndices.set(logicalModel.database.type, nextIndex + 1);
        const id = logicalModel.database.type + '-' + nextIndex;

        const mappings: GroupMapping[] = [];

        logicalModel.mappings.forEach(mapping => {
            const root = objects.find(object => object.key.equals(mapping.rootObjectKey));
            const properties = [ ...getObjectsFromPath(mapping.accessPath, context).values() ]
                .map(object => objects.find(o => o.key.equals(object.key)))
                .filter((object): object is VersionedSchemaObject => !!object);

            if (!root) {
                console.error('Root object not found for mapping', mapping);
                return;
            }

            mappings.push({ mapping, properties, root, groupId: id });
        });

        return {
            id,
            logicalModel,
            mappings,
        };
    });
}

function getObjectsFromPath(path: ParentProperty, context: Context): ComparableSet<SchemaObject, number> {
    const output: ComparableSet<SchemaObject, number> = new ComparableSet(object => object.key.value);

    path.subpaths.forEach(subpath => {
        findObjectsFromSignature(subpath.signature, context).forEach(object => output.add(object));

        if (subpath.name instanceof DynamicName)
            findObjectsFromSignature(subpath.name.signature, context).forEach(object => output.add(object));

        if (subpath instanceof ComplexProperty)
            getObjectsFromPath(subpath, context).forEach(object => output.add(object));
    });

    return output;
}

/** Finds all objects on the signature path except for the first one. */
function findObjectsFromSignature(signature: Signature, context: Context): SchemaObject[] {
    const output: SchemaObject[] = [];

    signature.toBases().forEach(rawBase => {
        const object = findObjectFromBaseSignature(rawBase, context);
        if (object)
            output.push(object);
    });

    return output;
}

function findObjectFromBaseSignature(rawBase: Signature, context: Context): SchemaObject | undefined {
    const base = rawBase.isBaseDual ? rawBase.dual() : rawBase;
    const morphism = context.morphisms.get(base);
    if (!morphism)
        return;

    return context.objects.get(rawBase.isBaseDual ? morphism.domKey : morphism.codKey);
}
