import { RootProperty, type RootPropertyJSON } from "./accessPath/basic";

export type MappingJSON = {
    kindName: string,
    pkey: string[],
    accessPath: RootPropertyJSON
}

export class Mapping {
    id: number | null;
    name: string;
    databaseId: number;
    rootObjectId: number;
    accessPath: RootProperty;

    private constructor(id: number | null, name: string, databaseId: number, rootObjectId: number, accessPath: RootProperty) {
        this.id = id;
        this.name = name;
        this.databaseId = databaseId;
        this.rootObjectId = rootObjectId;
        this.accessPath = accessPath;
    }

    static fromServer(input: MappingFromServer): Mapping {
        const json = JSON.parse(input.jsonValue) as { name: string };
        const mappingJson = JSON.parse(input.mappingJsonValue) as MappingJSON;
        const accessPath = RootProperty.fromJSON(mappingJson.accessPath);

        return new Mapping(input.id, json.name, input.databaseId, input.rootObjectId, accessPath);
    }
}

export class MappingFromServer {
    id!: number | null;
    databaseId!: number;
    categoryId!: number;
    rootObjectId!: number;
    jsonValue!: string;
    mappingJsonValue!: string;
}
