import { UniqueIdProvider } from '@/utils/UniqueIdProvider';
import { ComplexProperty, type ParentProperty } from '@/types/accessPath/basic';
import type { Entity, Id, VersionId } from '../id';
import { DynamicName, Key, type KeyFromServer, Signature, type SignatureFromServer } from '../identifiers';
import { type MetadataMorphismFromServer, SchemaMorphism, type SchemaMorphismFromServer, VersionedSchemaMorphism } from './Morphism';
import { type MetadataObjexFromServer, SchemaObjex, type SchemaObjexFromServer, VersionedSchemaObjex } from './Objex';
import type { Graph } from '../categoryGraph';
import { ComparableMap } from '@/utils/ComparableMap';
import type { Mapping } from '../mapping';
import { ComparableSet } from '@/utils/ComparableSet';
import type { DatasourceType, LogicalModel } from '../datasource';

export type SchemaCategoryFromServer = SchemaCategoryInfoFromServer & {
    schema: SerializedSchema;
    metadata: SerializedMetadata;
};

export type SerializedSchema = {
    objexes: SchemaObjexFromServer[];
    morphisms: SchemaMorphismFromServer[];
};

export type SerializedMetadata = {
    objexes: MetadataObjexFromServer[];
    morphisms: MetadataMorphismFromServer[];
};

export class Category implements Entity {
    private keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    private signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private readonly groups: GroupData[];

    private constructor(
        readonly id: Id,
        readonly label: string,
        readonly versionId: VersionId,
        readonly systemVersionId: VersionId,
        objexes: VersionedSchemaObjex[],
        morphisms: VersionedSchemaMorphism[],
        logicalModels: LogicalModel[],
    ) {
        objexes.forEach(objex => {
            if (!objex.current)
                return;

            this.objexes.set(objex.key, objex);
            this.keysProvider.add(objex.key);
        });

        morphisms.forEach(morphism => {
            if (!morphism.current)
                return;

            this.morphisms.set(morphism.signature, morphism);
            this.signatureProvider.add(morphism.signature);
        });

        this.groups = createGroups(logicalModels, objexes, morphisms);
        this.groups.forEach(group => {
            group.mappings.forEach(mapping => {
                mapping.properties.forEach(property => {
                    property.addGroup(group.id);
                });
                mapping.root.addGroup(group.id);
            });
        });
    }

    static fromServer(input: SchemaCategoryFromServer, logicalModels: LogicalModel[]): Category {
        const objexMetadata = new Map<KeyFromServer, MetadataObjexFromServer>(
            input.metadata.objexes.map(o => [ o.key, o ]),
        );
        const objexes = input.schema.objexes.map(o => VersionedSchemaObjex.fromServer(o, objexMetadata.get(o.key)!));

        const morphismMetadata = new Map<SignatureFromServer, MetadataMorphismFromServer>(
            input.metadata.morphisms.map(m => [ m.signature, m ]),
        );
        const morphisms = input.schema.morphisms.map(m => VersionedSchemaMorphism.fromServer(m, morphismMetadata.get(m.signature)!));

        return new Category(
            input.id,
            input.label,
            input.version,
            input.systemVersion,
            objexes,
            morphisms,
            logicalModels,
        );
    }

    static fromServerWithInfo(info: SchemaCategoryInfo, schema: SerializedSchema, metadata: SerializedMetadata): Category {
        return this.fromServer({ ...info, version: info.versionId, systemVersion: info.systemVersionId, schema, metadata }, []);
    }

    private objexes = new ComparableMap<Key, number, VersionedSchemaObjex>(key => key.value);
    private morphisms = new ComparableMap<Signature, string, VersionedSchemaMorphism>(signature => signature.value);

    createObjex(): VersionedSchemaObjex {
        const key = this.keysProvider.createAndAdd();
        return this.getObjex(key);
    }

    getObjex(key: Key): VersionedSchemaObjex {
        let objex = this.objexes.get(key);

        if (!objex) {
            objex = VersionedSchemaObjex.create(key, this._graph);
            this.objexes.set(key, objex);
            this.keysProvider.add(key);
        }

        return objex;
    }

    getObjexes(): VersionedSchemaObjex[] {
        return [ ...this.objexes.values() ];
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

    getMorphisms(): VersionedSchemaMorphism[] {
        return [ ...this.morphisms.values() ];
    }

    private _graph?: Graph;

    get graph(): Graph | undefined {
        return this._graph;
    }

    set graph(newGraph: Graph | undefined) {
        this._graph = newGraph;
        if (!newGraph) {
            this.objexes.forEach(objex => objex.graph = undefined);
            this.morphisms.forEach(morphism => morphism.graph = undefined);
            return;
        }

        newGraph.resetElements(this.groups);
        newGraph.batch(() => {
            this.objexes.forEach(objex => objex.graph = newGraph);
            this.morphisms.forEach(morphism => morphism.graph = newGraph);
        });

        // Position the objex to the center of the canvas.
        newGraph.fixLayout();
        newGraph.layout();
        newGraph.center();
    }
}

export type SchemaCategoryInfoFromServer = {
    id: Id;
    label: string;
    version: VersionId;
    systemVersion: VersionId;
};

export class SchemaCategoryInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly versionId: VersionId,
        public readonly systemVersionId: VersionId,
    ) {}

    static fromServer(input: SchemaCategoryInfoFromServer): SchemaCategoryInfo {
        return new SchemaCategoryInfo(
            input.id,
            input.label,
            input.version,
            input.systemVersion,
        );
    }
}

export type SchemaCategoryInit = {
    label: string;
};

export type GroupMapping = {
    mapping: Mapping;
    properties: VersionedSchemaObjex[];
    root: VersionedSchemaObjex;
    groupId: string;
};

export type GroupData = {
    id: string;
    logicalModel: LogicalModel;
    mappings: GroupMapping[];
};

type Context = {
    objexes: ComparableMap<Key, number, SchemaObjex>;
    morphisms: ComparableMap<Signature, string, SchemaMorphism>;
};

function createGroups(logicalModels: LogicalModel[], objexes: VersionedSchemaObjex[], morphisms: VersionedSchemaMorphism[]): GroupData[] {
    const context: Context = {
        objexes: new ComparableMap(key => key.value),
        morphisms: new ComparableMap(signature => signature.value),
    };

    objexes
        .map(objex => objex.current)
        .filter((o): o is SchemaObjex => !!o)
        .forEach(objex => context.objexes.set(objex.key, objex));

    morphisms
        .map(morphism => morphism.current)
        .filter((m): m is SchemaMorphism => !!m)
        .forEach(morphism => context.morphisms.set(morphism.signature, morphism));

    const typeIndices = new Map<DatasourceType, number>();

    return logicalModels.map(logicalModel => {
        const nextIndex = typeIndices.get(logicalModel.datasource.type) ?? 0;
        typeIndices.set(logicalModel.datasource.type, nextIndex + 1);
        const id = logicalModel.datasource.type + '-' + nextIndex;

        const mappings: GroupMapping[] = [];

        logicalModel.mappings.forEach(mapping => {
            const root = objexes.find(objex => objex.key.equals(mapping.rootObjexKey));
            const properties = [ ...getObjexesFromPath(mapping.accessPath, context).values() ]
                .map(objex => objexes.find(o => o.key.equals(objex.key)))
                .filter((objex): objex is VersionedSchemaObjex => !!objex);

            if (!root) {
                console.error('Root objex not found for mapping', mapping);
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

function getObjexesFromPath(path: ParentProperty, context: Context): ComparableSet<SchemaObjex, number> {
    const output: ComparableSet<SchemaObjex, number> = new ComparableSet(objex => objex.key.value);

    path.subpaths.forEach(subpath => {
        findObjexesFromSignature(subpath.signature, context).forEach(objex => output.add(objex));

        if (subpath.name instanceof DynamicName)
            findObjexesFromSignature(subpath.name.signature, context).forEach(objex => output.add(objex));

        if (subpath instanceof ComplexProperty)
            getObjexesFromPath(subpath, context).forEach(objex => output.add(objex));
    });

    return output;
}

/** Finds all objexes on the signature path except for the first one. */
function findObjexesFromSignature(signature: Signature, context: Context): SchemaObjex[] {
    const output: SchemaObjex[] = [];

    signature.toBases().forEach(rawBase => {
        const objex = findObjexFromBaseSignature(rawBase, context);
        if (objex)
            output.push(objex);
    });

    return output;
}

function findObjexFromBaseSignature(rawBase: Signature, context: Context): SchemaObjex | undefined {
    const base = rawBase.isBaseDual ? rawBase.dual() : rawBase;
    const morphism = context.morphisms.get(base);
    if (!morphism)
        return;

    return context.objexes.get(rawBase.isBaseDual ? morphism.domKey : morphism.codKey);
}
