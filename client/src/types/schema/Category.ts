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

/**
 * This class represents a schema category in a specific version.
 * It shouldn't be changed directly, but it can be updated via {@link Evocat}.
 */
export class Category implements Entity {
    private readonly groups: GroupData[] = [];

    private constructor(
        readonly id: Id,
        readonly label: string,
        readonly versionId: VersionId,
    ) {}

    static fromServer(input: SchemaCategoryFromServer, logicalModels: LogicalModel[]): Category {
        const category = new Category(
            input.id,
            input.label,
            input.version,
        );

        const objexMetadata = new Map<KeyFromServer, MetadataObjexFromServer>(
            input.metadata.objects.map(o => [ o.key, o ]),
        );
        const objexes = input.schema.objects.map(o => Objex.fromServer(o, objexMetadata.get(o.key)!));
        objexes.forEach(objex => {
            if (!objex.schema)
                return;

            category.objexes.set(objex.key, objex);
            category.keyProvider.add(objex.key);
        });

        const morphismMetadata = new Map<SignatureFromServer, MetadataMorphismFromServer>(
            input.metadata.morphisms.map(m => [ m.signature, m ]),
        );
        const morphisms = input.schema.morphisms.map(m => Morphism.fromServer(m, morphismMetadata.get(m.signature)!, category));
        morphisms.forEach(morphism => {
            if (!morphism.schema)
                return;

            category.morphisms.set(morphism.signature, morphism);
            category.signatureProvider.add(morphism.signature);
        });

        const groups = createGroups(logicalModels, objexes, morphisms);
        groups.forEach(group => {
            group.mappings.forEach(mapping => {
                mapping.properties.forEach(property => {
                    property.addGroup(group.id);
                });
                mapping.root.addGroup(group.id);
            });
            category.groups.push(group);
        });

        return category;
    }

    static fromServerWithInfo(info: SchemaCategoryInfo, schema: SerializedSchema, metadata: SerializedMetadata): Category {
        return this.fromServer({ ...info, version: info.versionId, systemVersion: info.systemVersionId, schema, metadata }, []);
    }

    private keyProvider = new UniqueIdProvider<Key>({
        function: key => key.value,
        inversion: value => Key.createNew(value),
    });

    createKey(): Key {
        return this.keyProvider.createAndAdd();
    }

    readonly objexes = new ComparableMap<Key, number, Objex>(key => key.value);

    getObjex(key: Key): Objex {
        return this.objexes.get(key)!;
    }

    getObjexes(): Objex[] {
        return [ ...this.objexes.values() ];
    }

    private signatureProvider = new UniqueIdProvider<Signature>({
        function: signature => signature.baseValue ?? 0,
        inversion: value => Signature.base(value),
    });

    createSignature(): Signature {
        return this.signatureProvider.createAndAdd();
    }

    readonly morphisms = new ComparableMap<Signature, string, Morphism>(signature => signature.value);

    getMorphism(signature: Signature): Morphism {
        return this.morphisms.get(signature)!;
    }

    getMorphisms(): Morphism[] {
        return [ ...this.morphisms.values() ];
    }
}

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
        .map(objex => objex.schema)
        .forEach(schemaObjex => context.objexes.set(schemaObjex.key, schemaObjex));

    morphisms
        .map(morphism => morphism.schema)
        .forEach(schemaMorphism => context.morphisms.set(schemaMorphism.signature, schemaMorphism));

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
