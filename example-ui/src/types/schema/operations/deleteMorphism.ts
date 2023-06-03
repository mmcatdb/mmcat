import type { SchemaCategory } from "../SchemaCategory";
import { SchemaMorphism, type SchemaMorphismFromServer } from "../SchemaMorphism";
import { type SMO, type SMOFromServer, SMOType } from "./schemaModificationOperation";

export type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
    //signature: SignatureFromServer; // TODO change on backend
    morphism: SchemaMorphismFromServer;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
    readonly type = SMOType.DeleteMorphism;

    private constructor(
        readonly morphism: SchemaMorphism,
    ) {}

    static fromServer(input: DeleteMorphismFromServer): DeleteMorphism {
        return new DeleteMorphism(
            SchemaMorphism.fromServer(input.morphism),
        );
    }

    static create(morphism: SchemaMorphism): DeleteMorphism {
        return new DeleteMorphism(
            morphism,
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
