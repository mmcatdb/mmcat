import { RootProperty } from "@/types/accessPath/basic";
import type { RootPropertyFromServer } from "./accessPath/serverTypes";
import type { Entity, Id } from "./id";
import { SignatureId, type SignatureIdFromServer } from "./identifiers";
import { SchemaObject, type SchemaObjectFromServer } from "./schema";

export type MappingFromServer = {
    id: Id;
    logicalModelId: Id;
    rootObject: SchemaObjectFromServer;
    primaryKey: SignatureIdFromServer;
    kindName: string;
    accessPath: RootPropertyFromServer;
};

export class Mapping implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string,
        public readonly logicalModelId: Id,
        public readonly rootObject: SchemaObject,
        public readonly primaryKey: SignatureId,
        public readonly accessPath: RootProperty
    ) {}

    static fromServer(input: MappingFromServer): Mapping {
        return new Mapping(
            input.id,
            input.kindName,
            input.logicalModelId,
            SchemaObject.fromServer(input.rootObject),
            SignatureId.fromServer(input.primaryKey),
            RootProperty.fromServer(input.accessPath)
        );
    }
}

export type MappingInit = Omit<MappingFromServer, 'id'>;

export type MappingInfoFromServer = {
    id: Id;
    kindName: string;
};

export class MappingInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly kindName: string
    ) {}

    static fromServer(input: MappingInfoFromServer): MappingInfo {
        return new MappingInfo(
            input.id,
            input.kindName
        );
    }
}
