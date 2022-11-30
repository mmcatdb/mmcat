import { DatabaseView, type DatabaseViewFromServer } from "./database";

export class LogicalModel {
    id: number;
    label: string;
    databaseView: DatabaseView;
    categoryId: number;

    private constructor(id: number, label: string, databaseView: DatabaseView, categoryId: number) {
        this.id = id;
        this.label = label;
        this.databaseView = databaseView;
        this.categoryId = categoryId;
    }

    static fromServer(input: LogicalModelFromServer): LogicalModel {
        const databaseView = DatabaseView.fromServer(input.databaseView);
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModel(input.id, json.label, databaseView, input.categoryId);
    }
}

export type LogicalModelFromServer = {
    id: number;
    databaseView: DatabaseViewFromServer;
    categoryId: number;
    jsonValue: string;
}
