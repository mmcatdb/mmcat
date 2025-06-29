import { RootProperty, type RootPropertyResponse } from '@/types/mapping';
import type { Entity, Id, VersionId } from '../id';
import { Key, SignatureId, type KeyResponse, type SignatureIdResponse } from '../identifiers';

export type MappingResponse = {
    id: Id;
    categoryId: Id;
    datasourceId: Id;
    rootObjexKey: KeyResponse;
    primaryKey: SignatureIdResponse;
    kindName: string;
    accessPath: RootPropertyResponse;
    version: VersionId;
};

export class Mapping implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly categoryId: Id,
        public readonly datasourceId: Id,
        public readonly rootObjexKey: Key,
        public readonly primaryKey: SignatureId,
        public readonly accessPath: RootProperty,
        public readonly version: VersionId,
    ) {}

    static fromResponse(input: MappingResponse): Mapping {
        return new Mapping(
            input.id,
            input.kindName,
            input.categoryId,
            input.datasourceId,
            Key.fromResponse(input.rootObjexKey),
            SignatureId.fromResponse(input.primaryKey),
            RootProperty.fromResponse(input.accessPath),
            input.version,
        );
    }
}

export type MappingInit = {
    categoryId: Id;
    datasourceId: Id;
    rootObjexKey: KeyResponse;
    primaryKey: SignatureIdResponse;
    kindName: string;
    accessPath: RootPropertyResponse;
};

export type MappingInfoResponse = {
    id: Id;
    kindName: string;
    version: VersionId;
};

export class MappingInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly version: VersionId,
    ) {}

    static fromResponse(input: MappingInfoResponse): MappingInfo {
        return new MappingInfo(
            input.id,
            input.kindName,
            input.version,
        );
    }
}
