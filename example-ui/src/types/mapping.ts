import { RootProperty } from "@/types/accessPath/basic";
import type { RootPropertyJSON } from "./accessPath/JSONTypes";
import type { Entity, Id } from "./id";
import { SignatureId, type SignatureIdFromServer } from "./identifiers";
import { SchemaObject, type SchemaObjectFromServer } from "./schema";
import { LogicalModelInfo, type LogicalModelInfoFromServer } from "./logicalModel";

export type MappingJSON = {
    kindName: string;
    primaryKey: SignatureIdFromServer;
    accessPath: RootPropertyJSON;
};

export class Mapping implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly logicalModelId: Id,
        public readonly rootObject: SchemaObject,
        public readonly primaryKey: SignatureId,
        public readonly accessPath: RootProperty
    ) {}

    static fromServer(input: MappingFromServer): Mapping {
        const json = JSON.parse(input.jsonValue) as { label: string };
        const mappingJson = JSON.parse(input.mappingJsonValue) as MappingJSON;

        return new Mapping(
            input.id,
            json.label,
            input.logicalModelId,
            SchemaObject.fromServer(input.rootObject),
            SignatureId.fromServer(mappingJson.primaryKey),
            RootProperty.fromJSON(mappingJson.accessPath)
        );
    }
}

export type MappingFromServer = {
    id: Id;
    logicalModelId: Id;
    rootObject: SchemaObjectFromServer;
    jsonValue: string;
    mappingJsonValue: string;
};

export type MappingInit = {
    logicalModelId: Id;
    rootObjectId: Id;
    mappingJsonValue: string;
    jsonValue: string;
};

export class MappingInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string
    ) {}

    static fromServer(input: MappingInfoFromServer): MappingInfo {
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new MappingInfo(
            input.id,
            json.label
        );
    }
}

export type MappingInfoFromServer = {
    id: Id;
    jsonValue: string;
};
