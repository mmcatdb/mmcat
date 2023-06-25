import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObject, type SchemaObjectFromServer } from '../SchemaObject';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

export type DeleteObjectFromServer = SMOFromServer<SMOType.DeleteObject> & {
    //key: KeyFromServer; // TODO change on backend
    object: SchemaObjectFromServer;
};

export class DeleteObject implements SMO<SMOType.DeleteObject> {
    readonly type = SMOType.DeleteObject;
    private readonly serialized: SchemaObjectFromServer;

    private constructor(
        readonly object: SchemaObject,
    ) {
        this.serialized = object.toServer();
    }

    static fromServer(input: DeleteObjectFromServer): DeleteObject {
        return new DeleteObject(
            SchemaObject.fromServer(input.object),
        );
    }

    static create(object: SchemaObject): DeleteObject {
        return new DeleteObject(
            object,
        );
    }

    toServer(): DeleteObjectFromServer {
        return {
            type: SMOType.DeleteObject,
            object: this.serialized,
        };
    }

    up(category: SchemaCategory): void {
        category.getObject(this.object.key).current = undefined;
    }

    down(category: SchemaCategory): void {
        category.getObject(this.object.key).current = this.object;
    }
}
