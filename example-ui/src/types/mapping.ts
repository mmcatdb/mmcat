import { RootProperty, type RootPropertyJSON } from "./accessPath/basic";
import type { Entity, Id } from "./id";
import { LogicalModelInfo, type LogicalModelInfoFromServer } from "./logicalModel";

export type MappingJSON = {
    kindName: string,
    pkey: string[],
    accessPath: RootPropertyJSON
}

export class Mapping implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly logicalModelId: Id,
        public readonly rootObjectId: Id,
        public readonly accessPath: RootProperty
    ) {}

    static fromServer(input: MappingFromServer): Mapping {
        const json = JSON.parse(input.jsonValue) as { label: string };
        const mappingJson = JSON.parse(input.mappingJsonValue) as MappingJSON;
        const accessPath = RootProperty.fromJSON(mappingJson.accessPath);

        return new Mapping(
            input.id,
            json.label,
            input.logicalModelId,
            input.rootObjectId,
            accessPath
        );
    }
}

export type MappingFromServer = {
    id: Id;
    logicalModelId: Id;
    rootObjectId: Id;
    jsonValue: string;
    mappingJsonValue: string;
}

export type MappingInit = {
    logicalModelId: Id;
    rootObjectId: Id;
    mappingJsonValue: string;
    jsonValue: string;
}

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
    id: Id,
    jsonValue: string
};
