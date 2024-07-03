import { Key, type KeyFromServer } from '@/types/identifiers';
import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObject, type SchemaObjectDataFromServer } from '../SchemaObject';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

export type CreateObjectFromServer = SMOFromServer<SMOType.CreateObject> & {
    key: KeyFromServer;
    object: SchemaObjectDataFromServer;
};

export class CreateObject implements SMO<SMOType.CreateObject> {
    readonly type = SMOType.CreateObject;

    private constructor(
        private readonly key: Key,
        private readonly object: SchemaObject,
    ) {}

    static fromServer(input: CreateObjectFromServer): CreateObject {
        return new CreateObject(
            Key.fromServer(input.key),
            SchemaObject.fromServer(input.key, input.object),
        );
    }

    static create(object: SchemaObject): CreateObject {
        return new CreateObject(
            object.key,
            object,
        );
    }

    toServer(): CreateObjectFromServer {
        return {
            type: SMOType.CreateObject,
            key: this.key.toServer(),
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
