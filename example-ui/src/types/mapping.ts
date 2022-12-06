import { RootProperty, type RootPropertyJSON } from "./accessPath/basic";
import { LogicalModelInfo, type LogicalModelInfoFromServer } from "./logicalModel";

export type MappingJSON = {
    kindName: string,
    pkey: string[],
    accessPath: RootPropertyJSON
}

export class Mapping {
    private constructor(
        public readonly id: number,
        public readonly label: string,
        public readonly logicalModelId: number,
        public readonly rootObjectId: number,
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
    id: number;
    logicalModelId: number;
    rootObjectId: number;
    jsonValue: string;
    mappingJsonValue: string;
}

export type MappingInit = {
    logicalModelId: number,
    rootObjectId: number,
    mappingJsonValue: string,
    jsonValue: string
}

export class MappingInfo {
    private constructor(
        public readonly id: number,
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
    id: number,
    jsonValue: string
};
