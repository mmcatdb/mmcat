import type { SchemaCategory } from '../SchemaCategory';
import { SchemaMorphism, type SchemaMorphismFromServer } from '../SchemaMorphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
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
        category.getMorphism(this.morphism.signature).current = undefined;
    }

    down(category: SchemaCategory): void {
        category.getMorphism(this.morphism.signature).current = this.morphism;
    }
}
