import { UniqueIdProvider } from '@/types/utils/UniqueIdProvider';
import { ComplexProperty, type ParentProperty } from '@/types/accessPath/basic';
import type { Entity, Id, VersionId } from '../id';
import { DynamicName, Key, type KeyFromServer, Signature, type SignatureFromServer } from '../identifiers';
import { type MetadataMorphismFromServer, type SchemaMorphism, type SchemaMorphismFromServer, Morphism } from './Morphism';
import { type MetadataObjexFromServer, type SchemaObjex, type SchemaObjexFromServer, Objex } from './Objex';
import { ComparableMap } from '@/types/utils/ComparableMap';
import type { Mapping } from '../mapping';
import { ComparableSet } from '@/types/utils/ComparableSet';
import type { DatasourceType, LogicalModel } from '../datasource';

export type SchemaCategoryFromServer = SchemaCategoryInfoFromServer & {
    schema: SerializedSchema;
    metadata: SerializedMetadata;
};

export type SerializedSchema = {
    objects: SchemaObjexFromServer[];
    morphisms: SchemaMorphismFromServer[];
};

export type SerializedMetadata = {
    objects: MetadataObjexFromServer[];
    morphisms: MetadataMorphismFromServer[];
};

export class SchemaCategory implements Entity {
    private keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    private signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private readonly groups: GroupData[];

    private constructor(
        readonly id: Id,
        readonly label: string,
        readonly versionId: VersionId,
        objexes: Objex[],
        morphisms: Morphism[],
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

    static fromServer(input: SchemaCategoryFromServer, logicalModels: LogicalModel[]): SchemaCategory {
        const objexMetadata = new Map<KeyFromServer, MetadataObjexFromServer>(
            input.metadata.objects.map(o => [ o.key, o ]),
        );
        const objexes = input.schema.objects.map(o => Objex.fromServer(o, objexMetadata.get(o.key)!));

        const morphismMetadata = new Map<SignatureFromServer, MetadataMorphismFromServer>(
            input.metadata.morphisms.map(m => [ m.signature, m ]),
        );
        const morphisms = input.schema.morphisms.map(m => Morphism.fromServer(m, morphismMetadata.get(m.signature)!));

        return new SchemaCategory(
            input.id,
            input.label,
            input.version,
            objexes,
            morphisms,
            logicalModels,
        );
    }

    static fromServerWithInfo(info: SchemaCategoryInfo, schema: SerializedSchema, metadata: SerializedMetadata): SchemaCategory {
        return this.fromServer({ ...info, version: info.versionId, systemVersion: info.systemVersionId, schema, metadata }, []);
    }

    private readonly objexes = new ComparableMap<Key, number, Objex>(key => key.value);
    private readonly morphisms = new ComparableMap<Signature, string, Morphism>(signature => signature.value);

    createObjex(): Objex {
        const key = this.keysProvider.createAndAdd();
        return this.getObjex(key);
    }

    getObjex(key: Key): Objex {
        let objex = this.objexes.get(key);

        if (!objex) {
            objex = Objex.create(key);
            this.objexes.set(key, objex);
            this.keysProvider.add(key);
        }

        return objex;
    }

    getObjexes(): Objex[] {
        return [ ...this.objexes.values() ];
    }

    createMorphism(): Morphism {
        const signature = this.signatureProvider.createAndAdd();
        return this.getMorphism(signature);
    }

    getMorphism(signature: Signature): Morphism {
        let morphism = this.morphisms.get(signature);

        if (!morphism) {
            morphism = Morphism.create(signature);
            this.morphisms.set(signature, morphism);
            this.signatureProvider.add(signature);
        }

        return morphism;
    }

    getMorphisms(): Morphism[] {
        return [ ...this.morphisms.values() ];
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
    properties: Objex[];
    root: Objex;
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

function createGroups(logicalModels: LogicalModel[], objexes: Objex[], morphisms: Morphism[]): GroupData[] {
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
                .filter((objex): objex is Objex => !!objex);

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

function getObjexesFromPath(path: ParentProperty, context: Context): ComparableSet<SchemaObjex, number> {
    const output = new ComparableSet<SchemaObjex, number>(objex => objex.key.value);

    path.subpaths.forEach(subpath => {
        findObjexesFromSignature(subpath.signature, context).forEach(objex => output.add(objex));

        if (subpath.name instanceof DynamicName)
            findObjexesFromSignature(subpath.name.signature, context).forEach(objex => output.add(objex));

        if (subpath instanceof ComplexProperty)
            getObjexesFromPath(subpath, context).forEach(objex => output.add(objex));
    });

    return output;
}

/** Finds all objects on the signature path except for the first one. */
function findObjexesFromSignature(signature: Signature, context: Context): SchemaObjex[] {
    const output: SchemaObjex[] = [];

    signature.toBases().forEach(rawBase => {
        const objex = findObjexesFromBaseSignature(rawBase, context);
        if (objex)
            output.push(objex);
    });

    return output;
}

function findObjexesFromBaseSignature(rawBase: Signature, context: Context): SchemaObjex | undefined {
    const base = rawBase.isBaseDual ? rawBase.dual() : rawBase;
    const morphism = context.morphisms.get(base);
    if (!morphism)
        return;

    return context.objexes.get(rawBase.isBaseDual ? morphism.domKey : morphism.codKey);
}
