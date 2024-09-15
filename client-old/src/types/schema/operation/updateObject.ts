import type { SchemaCategory } from '../SchemaCategory';
import { SchemaObject, type SchemaObjectFromServer } from '../SchemaObject';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type UpdateObjectFromServer = SMOFromServer<SMOType.UpdateObject> & {
    newObject: SchemaObjectFromServer;
    oldObject: SchemaObjectFromServer;
};

export class UpdateObject implements SMO<SMOType.UpdateObject> {
    readonly type = SMOType.UpdateObject;

    private constructor(
        readonly newObject: SchemaObject,
        readonly oldObject: SchemaObject,
    ) {}

    static fromServer(input: UpdateObjectFromServer): UpdateObject {
        return new UpdateObject(
            SchemaObject.fromServer(input.newObject),
            SchemaObject.fromServer(input.oldObject),
        );
    }

    static create(newObject: SchemaObject, oldObject: SchemaObject): UpdateObject {
        if (!newObject.key.equals(oldObject.key))
            throw new Error('Cannot edit object\'s key.');

        return new UpdateObject(
            newObject,
            oldObject,
        );
    }

    toServer(): UpdateObjectFromServer {
        return {
            type: SMOType.UpdateObject,
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
