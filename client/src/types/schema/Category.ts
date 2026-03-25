import { UniqueIdProvider } from '@/types/utils/UniqueIdProvider';
import type { Entity, Id, VersionId } from '../id';
import { Key, type KeyResponse, Signature, type SignatureResponse } from '../identifiers';
import { type MetadataMorphismResponse, type SchemaMorphismResponse, Morphism } from './Morphism';
import { type MetadataObjexResponse, type SchemaObjexResponse, Objex } from './Objex';
import { ComparableMap } from '@/types/utils/ComparableMap';

/**
 * This class represents a schema category in a specific version.
 * It shouldn't be changed directly, but it can be updated via {@link Evocat}.
 */
export class Category implements Entity {
    private constructor(
        readonly id: Id,
        readonly versionId: VersionId,
        readonly example: string | undefined,
        readonly label: string,
    ) {}

    static fromResponse(input: CategoryResponse): Category {
        const category = new Category(
            input.id,
            input.version,
            input.example ?? undefined,
            input.label,
        );

        const objexMetadata = new Map<KeyResponse, MetadataObjexResponse>(
            input.metadata.objexes.map(metadata => [ metadata.key, metadata ]),
        );
        input.schema.objexes.forEach(sr => {
            const objex = Objex.fromResponse(category, sr, objexMetadata.get(sr.key)!);
            category.objexes.set(objex.key, objex);
            category.keyProvider.add(objex.key);
        });

        const morphismMetadata = new Map<SignatureResponse, MetadataMorphismResponse>(
            input.metadata.morphisms.map(metadata => [ metadata.signature, metadata ]),
        );
        input.schema.morphisms.forEach(sr => {
            const morphism = Morphism.fromResponse(category, sr, morphismMetadata.get(sr.signature)!);
            category.morphisms.set(morphism.signature, morphism);
            category.signatureProvider.add(morphism.signature);
        });

        return category;
    }

    private keyProvider = new UniqueIdProvider<Key>({
        function: key => key.value,
        inversion: value => Key.fromNumber(value),
    });

    createKey(): Key {
        return this.keyProvider.createAndAdd();
    }

    /** @internal */
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

    /** @internal */
    readonly morphisms = new ComparableMap<Signature, string, Morphism>(signature => signature.value);

    getMorphism(signature: Signature): Morphism {
        return this.morphisms.get(signature)!;
    }

    getMorphisms(): Morphism[] {
        return [ ...this.morphisms.values() ];
    }

    getEdge(signature: Signature): { morphism: Morphism, direction: boolean } {
        const morphism = this.morphisms.get(signature);
        if (morphism)
            return { morphism, direction: true };

        return { morphism: this.getMorphism(signature.dual()), direction: false };
    }
}

export type CategoryResponse = CategoryInfoResponse & {
    schema: SerializedSchema;
    metadata: SerializedMetadata;
};

export type SerializedSchema = {
    objexes: SchemaObjexResponse[];
    morphisms: SchemaMorphismResponse[];
};

export type SerializedMetadata = {
    objexes: MetadataObjexResponse[];
    morphisms: MetadataMorphismResponse[];
};

export type CategoryInfoResponse = {
    id: Id;
    version: VersionId;
    systemVersion: VersionId;
    example: string | null;
    label: string;
};

export class CategoryInfo implements Entity {
    private constructor(
        readonly id: Id,
        readonly versionId: VersionId,
        readonly systemVersionId: VersionId,
        readonly example: string | undefined,
        readonly label: string,
    ) {}

    static fromResponse(input: CategoryInfoResponse): CategoryInfo {
        return new CategoryInfo(
            input.id,
            input.version,
            input.systemVersion,
            input.example ?? undefined,
            input.label,
        );
    }
}

export type CategoryInit = {
    label: string;
};

export type CategoryStats = {
    objexes: number;
    mappings: number;
    jobs: number;
};

export enum Example {
    basic = 'basic',
    adminer = 'adminer',
    queryEvolution = 'query-evolution',
    inference = 'inference',
    tpch = 'tpch',
    adaptation = 'adaptation',
}
