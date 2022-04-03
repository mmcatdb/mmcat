import { DatabaseConfiguration, type DatabaseConfigurationFromServer } from "./Configuration";

export class Database {
    public readonly id: number;
    public readonly type: string;
    public readonly label: string;
    public configuration: DatabaseConfiguration;

    public constructor(fromServer: DatabaseFromServer) {
        this.id = fromServer.id;
        this.type = fromServer.type;
        this.label = fromServer.label;
        this.configuration = new DatabaseConfiguration(fromServer.configuration);
    }
}

export type DatabaseFromServer = {
    id: number;
    type: string; // Full type (i.e. MongoDB)
    label: string; // User-defined name
    configuration: DatabaseConfigurationFromServer;
}
