import { Key, type KeyFromServer } from '@/types/identifiers';
import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObject, type SchemaObjectDataFromServer } from '../SchemaObject';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

export type EditObjectFromServer = SMOFromServer<SMOType.EditObject> & {
    key: KeyFromServer;
    newObject: SchemaObjectDataFromServer;
    oldObject: SchemaObjectDataFromServer;
};

export class EditObject implements SMO<SMOType.EditObject> {
    readonly type = SMOType.EditObject;

    private constructor(
        private readonly key: Key,
        readonly newObject: SchemaObject,
        readonly oldObject: SchemaObject,
    ) {}

    static fromServer(input: EditObjectFromServer): EditObject {
        return new EditObject(
            Key.fromServer(input.key),
            SchemaObject.fromServer(input.key, input.newObject),
            SchemaObject.fromServer(input.key, input.oldObject),
        );
    }

    static create(newObject: SchemaObject, oldObject: SchemaObject): EditObject {
        if (!newObject.key.equals(oldObject.key))
            throw new Error('Cannot edit object\'s key.');

        return new EditObject(
            newObject.key,
            newObject,
            oldObject,
        );
    }

    toServer(): EditObjectFromServer {
        return {
            type: SMOType.EditObject,
            key: this.key.toServer(),
            newObject: this.newObject.toServer(),
            oldObject: this.oldObject.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.getObject(this.newObject.key).current = this.newObject;
    }

    down(category: SchemaCategory): void {
        category.getObject(this.oldObject.key).current = this.oldObject;
    }
}
