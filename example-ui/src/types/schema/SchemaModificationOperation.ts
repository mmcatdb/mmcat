import { Key, Signature, type KeyFromServer, type SignatureFromServer } from "../identifiers";
import type { SchemaCategory } from "./SchemaCategory";
import { SchemaMorphism, type SchemaMorphismFromServer } from "./SchemaMorphism";
import { SchemaObject, type SchemaObjectFromServer } from "./SchemaObject";

enum SMOType {
    AddObject = 'addObject',
    DeleteObject = 'deleteObject',
    AddMorphism = 'addMorphism',
    DeleteMorphism = 'deleteMorphism',
}

export type SMOFromServer<T extends SMOType = SMOType> = {
    type: T;
};

export interface SMO<T extends SMOType = SMOType> {
    readonly type: T;
    toServer(): SMOFromServer<T> | null;
    up(category: SchemaCategory): void;
    down(category: SchemaCategory): void;
}

type AddObjectFromServer = SMOFromServer<SMOType.AddObject> & {
    object: SchemaObjectFromServer;
};

export class AddObject implements SMO<SMOType.AddObject> {
    readonly type = SMOType.AddObject;

    constructor(
        readonly object: SchemaObject,
    ) {}

    static fromServer(input: AddObjectFromServer): AddObject {
        return new AddObject(
            SchemaObject.fromServer(input.object),
        );
    }

    toServer(): AddObjectFromServer | null {
        const object = this.object.toServer();
        if (!object)
            return null;

        return {
            type: SMOType.AddObject,
            object,
        };
    }

    up(category: SchemaCategory): void {
        category.addObject(this.object);
    }

    down(category: SchemaCategory): void {
        category.removeObject(this.object);
    }
}

type DeleteObjectFromServer = SMOFromServer<SMOType.DeleteObject> & {
    key: KeyFromServer;
};

export class DeleteObject implements SMO<SMOType.DeleteObject> {
    readonly type = SMOType.DeleteObject;

    constructor(
        readonly key: Key,
    ) {}

    static fromServer(input: DeleteObjectFromServer): DeleteObject {
        return new DeleteObject(
            Key.fromServer(input.key),
        );
    }

    toServer(): DeleteObjectFromServer | null {
        return {
            type: SMOType.DeleteObject,
            key: this.key.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        const object = category.objects.find(o => o.key.equals(this.key));
        if (object)
            category.removeObject(object);
    }

    down(category: SchemaCategory): void {
        console.log('TODO up ' + this.type);
    }
}

type AddMorphismFromServer = SMOFromServer<SMOType.AddMorphism> & {
    morphism: SchemaMorphismFromServer;
};

export class AddMorphism implements SMO<SMOType.AddMorphism> {
    readonly type = SMOType.AddMorphism;

    constructor(
        readonly morphism: SchemaMorphism,
    ) {}

    static fromServer(input: AddMorphismFromServer): AddMorphism {
        return new AddMorphism(
            SchemaMorphism.fromServer(input.morphism),
        );
    }

    toServer(): AddMorphismFromServer {
        return {
            type: SMOType.AddMorphism,
            morphism: this.morphism.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        console.log('TODO up ' + this.type);
    }

    down(category: SchemaCategory): void {
        console.log('TODO up ' + this.type);
    }
}

type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
    signature: SignatureFromServer;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
    readonly type = SMOType.DeleteMorphism;

    constructor(
        readonly signature: Signature,
    ) {}

    static fromServer(input: DeleteMorphismFromServer): DeleteMorphism {
        return new DeleteMorphism(
            Signature.fromServer(input.signature),
        );
    }

    toServer(): DeleteMorphismFromServer {
        return {
            type: SMOType.DeleteMorphism,
            signature: this.signature.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        console.log('TODO up ' + this.type);
    }

    down(category: SchemaCategory): void {
        console.log('TODO up ' + this.type);
    }
}

export function fromServer(input: SMOFromServer): SMO {
    switch (input.type) {
    case SMOType.AddObject:
        return AddObject.fromServer(input as AddObjectFromServer);
    case SMOType.DeleteObject:
        return DeleteObject.fromServer(input as DeleteObjectFromServer);
    case SMOType.AddMorphism:
        return AddMorphism.fromServer(input as AddMorphismFromServer);
    case SMOType.DeleteMorphism:
        return DeleteMorphism.fromServer(input as DeleteMorphismFromServer);
    }
}

