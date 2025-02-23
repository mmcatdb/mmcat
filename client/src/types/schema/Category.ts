import { UniqueIdProvider } from '@/types/utils/UniqueIdProvider';
import type { Entity, Id, VersionId } from '../id';
import { Key, type KeyFromServer, Signature, type SignatureFromServer } from '../identifiers';
import { type MetadataMorphismFromServer, type SchemaMorphismFromServer, Morphism } from './Morphism';
import { type MetadataObjexFromServer, type SchemaObjexFromServer, Objex } from './Objex';
import { ComparableMap } from '@/types/utils/ComparableMap';

/**
 * This class represents a schema category in a specific version.
 * It shouldn't be changed directly, but it can be updated via {@link Evocat}.
 */
export class Category implements Entity {
    private constructor(
        readonly id: Id,
        readonly label: string,
        readonly versionId: VersionId,
    ) {}

    static fromServer(input: SchemaCategoryFromServer): Category {
        const category = new Category(
            input.id,
            input.label,
            input.version,
        );

        const objexMetadata = new Map<KeyFromServer, MetadataObjexFromServer>(
            input.metadata.objects.map(metadata => [ metadata.key, metadata ]),
        );
        const objexes = input.schema.objects.map(schema => Objex.fromServer(category, schema, objexMetadata.get(schema.key)!));
        objexes.forEach(objex => {
            if (!objex.schema)
                return;

            category.objexes.set(objex.key, objex);
            category.keyProvider.add(objex.key);
        });

        const morphismMetadata = new Map<SignatureFromServer, MetadataMorphismFromServer>(
            input.metadata.morphisms.map(metadata => [ metadata.signature, metadata ]),
        );
        const morphisms = input.schema.morphisms.map(schema => Morphism.fromServer(category, schema, morphismMetadata.get(schema.signature)!));
        morphisms.forEach(morphism => {
            if (!morphism.schema)
                return;

            category.morphisms.set(morphism.signature, morphism);
            category.signatureProvider.add(morphism.signature);
        });

        return category;
    }

    static fromServerWithInfo(info: SchemaCategoryInfo, schema: SerializedSchema, metadata: SerializedMetadata): Category {
        return this.fromServer({ ...info, version: info.versionId, systemVersion: info.systemVersionId, schema, metadata });
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
