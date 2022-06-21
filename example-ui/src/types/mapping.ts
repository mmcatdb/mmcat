import { RootProperty, type RootPropertyJSON } from "./accessPath/basic";
import { DatabaseView, type DatabaseViewFromServer } from "./database";

export type MappingJSON = {
    kindName: string,
    pkey: string[],
    accessPath: RootPropertyJSON
}

export class Mapping {
    id: number;
    name: string;
    databaseView: DatabaseView;
    rootObjectId: number;
    accessPath: RootProperty;

    private constructor(id: number, name: string, databaseView: DatabaseView, rootObjectId: number, accessPath: RootProperty) {
        this.id = id;
        this.name = name;
        this.databaseView = databaseView;
        this.rootObjectId = rootObjectId;
        this.accessPath = accessPath;
    }

    static fromServer(input: MappingFromServer): Mapping {
        const databaseView = new DatabaseView(input.databaseView);
        const json = JSON.parse(input.jsonValue) as { name: string };
        const mappingJson = JSON.parse(input.mappingJsonValue) as MappingJSON;
        const accessPath = RootProperty.fromJSON(mappingJson.accessPath);

        return new Mapping(input.id, json.name, databaseView, input.rootObjectId, accessPath);
    }
}

export type MappingFromServer = {
    id: number;
    databaseView: DatabaseViewFromServer;
    categoryId: number;
    rootObjectId: number;
    jsonValue: string;
    mappingJsonValue: string;
}
