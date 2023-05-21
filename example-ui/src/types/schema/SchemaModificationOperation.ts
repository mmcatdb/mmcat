import type { SchemaCategory } from "./SchemaCategory";
import { SchemaMorphism, type SchemaMorphismFromServer } from "./SchemaMorphism";
import { SchemaObject, type SchemaObjectFromServer } from "./SchemaObject";

enum SMOType {
    AddObject = 'addObject',
    DeleteObject = 'deleteObject',
    AddMorphism = 'addMorphism',
    DeleteMorphism = 'deleteMorphism',
    Composite = 'composite',
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
    private readonly serialized: SchemaObjectFromServer;

    private constructor(
        readonly object: SchemaObject,
    ) {
        this.serialized = object.toServer();
    }

    static fromServer(input: AddObjectFromServer): AddObject {
        return new AddObject(
            SchemaObject.fromServer(input.object),
        );
    }

    static create(object: SchemaObject): AddObject {
        return new AddObject(
            object,
        );
    }

    toServer(): AddObjectFromServer | null {
        return {
            type: SMOType.AddObject,
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

type DeleteObjectFromServer = SMOFromServer<SMOType.DeleteObject> & {
    //key: KeyFromServer; // TODO change on backend
    object: SchemaObjectFromServer;
};

export class DeleteObject implements SMO<SMOType.DeleteObject> {
    readonly type = SMOType.DeleteObject;
    private readonly serialized: SchemaObjectFromServer;

    constructor(
        readonly object: SchemaObject,
    ) {
        this.serialized = object.toServer();
    }

    static fromServer(input: DeleteObjectFromServer): DeleteObject {
        return new DeleteObject(
            SchemaObject.fromServer(input.object),
        );
    }

    toServer(): DeleteObjectFromServer | null {
        return {
            type: SMOType.DeleteObject,
            object: this.serialized,
        };
    }

    up(category: SchemaCategory): void {
        category.removeObject(this.object);
    }

    down(category: SchemaCategory): void {
        category.addObject(this.object);
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
        category.addMorphism(this.morphism);
    }

    down(category: SchemaCategory): void {
        category.removeMorphism(this.morphism);
    }
}

type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
    //signature: SignatureFromServer; // TODO change on backend
    morphism: SchemaMorphismFromServer;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
    readonly type = SMOType.DeleteMorphism;

    constructor(
        readonly morphism: SchemaMorphism,
    ) {}

    static fromServer(input: DeleteMorphismFromServer): DeleteMorphism {
        return new DeleteMorphism(
            SchemaMorphism.fromServer(input.morphism),
        );
    }

    toServer(): DeleteMorphismFromServer {
        return {
            type: SMOType.DeleteMorphism,
            morphism: this.morphism.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.removeMorphism(this.morphism);
    }

    down(category: SchemaCategory): void {
        category.addMorphism(this.morphism);
    }
}

type CompositeFromServer = SMOFromServer<SMOType.Composite> & {
    name: string;
};

export class Composite implements SMO<SMOType.Composite> {
    readonly type = SMOType.Composite;

    constructor(
        readonly name: string,
    ) {}

    static fromServer(input: CompositeFromServer): Composite {
        return new Composite(
            input.name,
        );
    }

    toServer(): CompositeFromServer {
        return {
            type: SMOType.Composite,
            name: this.name,
        };
    }

    up(): void {
        /* This function is intentionally empty. */
    }

    down(): void {
        /* This function is intentionally empty. */
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
    case SMOType.Composite:
        return Composite.fromServer(input as CompositeFromServer);
    }
}

