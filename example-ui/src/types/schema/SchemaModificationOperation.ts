import { Key, Signature, type KeyFromServer, type SignatureFromServer } from "../identifiers";
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
    toServer(): SMOFromServer<T> | null;
}

type AddObjectFromServer = SMOFromServer<SMOType.AddObject> & {
    object: SchemaObjectFromServer;
};

export class AddObject implements SMO<SMOType.AddObject> {
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
}

type DeleteObjectFromServer = SMOFromServer<SMOType.DeleteObject> & {
    key: KeyFromServer;
};

export class DeleteObject implements SMO<SMOType.DeleteObject> {
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
}

type AddMorphismFromServer = SMOFromServer<SMOType.AddMorphism> & {
    morphism: SchemaMorphismFromServer;
};

export class AddMorphism implements SMO<SMOType.AddMorphism> {
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
}

type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
    signature: SignatureFromServer;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
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

