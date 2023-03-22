import { SchemaMorphism, type SchemaMorphismFromServer } from "./SchemaMorphism";
import { SchemaObject, type SchemaObjectFromServer } from "./SchemaObject";

enum SMOType {
    AddObject = 'addObject',
    AddMorphism = 'addMorphism'
}

export type SMOFromServer<T extends SMOType = SMOType> = {
    type: T;
};

export interface SMO<T extends SMOType = SMOType> {
    toServer(): SMOFromServer<T>;
}

type AddObjectFromServer = SMOFromServer<SMOType.AddObject> & {
    object: SchemaObjectFromServer;
};

export class AddObject implements SMO<SMOType.AddObject> {
    constructor(
        readonly object: SchemaObject
    ) {}

    static fromServer(input: AddObjectFromServer): AddObject {
        return new AddObject(
            SchemaObject.fromServer(input.object)
        );
    }

    toServer(): AddObjectFromServer {
        return {
            type: SMOType.AddObject,
            object: this.object.toServer()
        };
    }
}

type AddMorphismFromServer = SMOFromServer<SMOType.AddMorphism> & {
    morphism: SchemaMorphismFromServer;
};

export class AddMorphism implements SMO<SMOType.AddMorphism> {
    constructor(
        readonly morphism: SchemaMorphism
    ) {}

    static fromServer(input: AddMorphismFromServer): AddMorphism {
        return new AddMorphism(
            SchemaMorphism.fromServer(input.morphism)
        );
    }

    toServer(): AddMorphismFromServer {
        return {
            type: SMOType.AddMorphism,
            morphism: this.morphism.toServer()
        };
    }
}

export function fromServer(input: SMOFromServer): SMO {
    switch (input.type) {
    case SMOType.AddObject:
        return AddObject.fromServer(input as AddObjectFromServer);
    case SMOType.AddMorphism:
        return AddMorphism.fromServer(input as AddMorphismFromServer);
    }
}
