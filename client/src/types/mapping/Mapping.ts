import { RootProperty, type ComplexPropertyResponse } from '@/types/mapping';
import type { Entity, Id, VersionId } from '../id';
import { Key, SignatureId, type KeyResponse, type SignatureIdResponse } from '../identifiers';

export type MappingResponse = {
    id: Id;
    categoryId: Id;
    datasourceId: Id;
    rootObjexKey: KeyResponse;
    primaryKey: SignatureIdResponse;
    kindName: string;
    accessPath: ComplexPropertyResponse;
    version: VersionId;
};

export class Mapping implements Entity {
    private constructor(
        readonly id: Id,
        readonly kindName: string,
        readonly categoryId: Id,
        readonly datasourceId: Id,
        readonly rootObjexKey: Key,
        readonly primaryKey: SignatureId,
        readonly accessPath: RootProperty,
        readonly version: VersionId,
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
    accessPath: ComplexPropertyResponse;
};

export type MappingEdit = {
    primaryKey: SignatureIdResponse;
    kindName: string;
    accessPath: ComplexPropertyResponse;
};

export type MappingInfoResponse = {
    id: Id;
    kindName: string;
    version: VersionId;
};

export class MappingInfo implements Entity {
    private constructor(
        readonly id: Id,
        readonly kindName: string,
        readonly version: VersionId,
    ) {}

    static fromResponse(input: MappingInfoResponse): MappingInfo {
        return new MappingInfo(
            input.id,
            input.kindName,
            input.version,
        );
    }
}
