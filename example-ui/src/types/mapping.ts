import { RootProperty, type RootPropertyJSON } from "./accessPath/basic";
import { LogicalModel, type LogicalModelFromServer } from "./logicalModel";

export type MappingJSON = {
    kindName: string,
    pkey: string[],
    accessPath: RootPropertyJSON
}

export class Mapping {
    id: number;
    name: string;
    logicalModel: LogicalModel;
    rootObjectId: number;
    accessPath: RootProperty;

    private constructor(id: number, name: string, logicalModel: LogicalModel, rootObjectId: number, accessPath: RootProperty) {
        this.id = id;
        this.name = name;
        this.logicalModel = logicalModel;
        this.rootObjectId = rootObjectId;
        this.accessPath = accessPath;
    }

    static fromServer(input: MappingFromServer): Mapping {
        const logicalModel = LogicalModel.fromServer(input.logicalModelView);
        const json = JSON.parse(input.jsonValue) as { name: string };
        const mappingJson = JSON.parse(input.mappingJsonValue) as MappingJSON;
        const accessPath = RootProperty.fromJSON(mappingJson.accessPath);

        return new Mapping(input.id, json.name, logicalModel, input.rootObjectId, accessPath);
    }
}

export type MappingFromServer = {
    id: number;
    logicalModelView: LogicalModelFromServer;
    rootObjectId: number;
    jsonValue: string;
    mappingJsonValue: string;
}
