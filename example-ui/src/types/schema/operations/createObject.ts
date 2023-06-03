import type { SchemaCategory } from "../SchemaCategory";
import { SchemaObject, type SchemaObjectFromServer } from "../SchemaObject";
import { type SMO, type SMOFromServer, SMOType } from "./schemaModificationOperation";

export type CreateObjectFromServer = SMOFromServer<SMOType.CreateObject> & {
    object: SchemaObjectFromServer;
};

export class CreateObject implements SMO<SMOType.CreateObject> {
    readonly type = SMOType.CreateObject;
    private readonly serialized: SchemaObjectFromServer;

    private constructor(
        readonly object: SchemaObject,
    ) {
        this.serialized = object.toServer();
    }

    static fromServer(input: CreateObjectFromServer): CreateObject {
        return new CreateObject(
            SchemaObject.fromServer(input.object),
        );
    }

    static create(object: SchemaObject): CreateObject {
        return new CreateObject(
            object,
        );
    }

    toServer(): CreateObjectFromServer {
        return {
            type: SMOType.CreateObject,
            object: this.serialized,
        };
    }

    up(category: SchemaCategory): void {
        category.addObject(this.object);
    }

    down(category: SchemaCategory): void {
        category.removeObject(this.object);
    }
}
