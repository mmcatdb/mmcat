import { Key, type KeyFromServer } from '@/types/identifiers';
import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObject, type SchemaObjectDataFromServer } from '../SchemaObject';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

export type DeleteObjectFromServer = SMOFromServer<SMOType.DeleteObject> & {
    key: KeyFromServer;
    object: SchemaObjectDataFromServer;
};

export class DeleteObject implements SMO<SMOType.DeleteObject> {
    readonly type = SMOType.DeleteObject;

    private constructor(
        private readonly key: Key,
        private readonly object: SchemaObject,
    ) {}

    static fromServer(input: DeleteObjectFromServer): DeleteObject {
        return new DeleteObject(
            Key.fromServer(input.key),
            SchemaObject.fromServer(input.key, input.object),
        );
    }

    static create(object: SchemaObject): DeleteObject {
        return new DeleteObject(
            object.key,
            object,
        );
    }

    toServer(): DeleteObjectFromServer {
        return {
            type: SMOType.DeleteObject,
            key: this.key.toServer(),
            object: this.object.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.getObject(this.object.key).current = undefined;
    }

    down(category: SchemaCategory): void {
        category.getObject(this.object.key).current = this.object;
    }
}
