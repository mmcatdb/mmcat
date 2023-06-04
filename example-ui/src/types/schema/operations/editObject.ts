import type { SchemaCategory } from "../SchemaCategory";
import { SchemaObject, type SchemaObjectFromServer } from "../SchemaObject";
import { type SMO, type SMOFromServer, SMOType } from "./schemaModificationOperation";

export type EditObjectFromServer = SMOFromServer<SMOType.EditObject> & {
    newObject: SchemaObjectFromServer;
    oldObject: SchemaObjectFromServer;
};

export class EditObject implements SMO<SMOType.EditObject> {
    readonly type = SMOType.EditObject;

    private constructor(
        readonly newObject: SchemaObject,
        readonly oldObject: SchemaObject,
    ) {}

    static fromServer(input: EditObjectFromServer): EditObject {
        return new EditObject(
            SchemaObject.fromServer(input.newObject),
            SchemaObject.fromServer(input.oldObject),
        );
    }

    static create(newObject: SchemaObject, oldObject: SchemaObject): EditObject {
        if (!newObject.key.equals(oldObject.key))
            throw new Error('Cannot edit object\'s key.');

        if (newObject.ids !== oldObject.ids)
            throw new Error('Cannot edit object\'s ids.');

        return new EditObject(
            newObject,
            oldObject,
        );
    }

    toServer(): EditObjectFromServer {
        return {
            type: SMOType.EditObject,
            newObject: this.newObject.toServer(),
            oldObject: this.oldObject.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.editObject(this.newObject);
    }

    down(category: SchemaCategory): void {
        category.editObject(this.oldObject);
    }
}
