import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObject, type SchemaObjectFromServer } from '../SchemaObject';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

export type CreateObjectFromServer = SMOFromServer<SMOType.CreateObject> & {
    object: SchemaObjectFromServer;
};

export class CreateObject implements SMO<SMOType.CreateObject> {
    readonly type = SMOType.CreateObject;

    private constructor(
        private readonly object: SchemaObject,
    ) {}

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
            object: this.object.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.getObject(this.object.key).current = this.object;
    }

    down(category: SchemaCategory): void {
        category.getObject(this.object.key).current = undefined;
    }
}
