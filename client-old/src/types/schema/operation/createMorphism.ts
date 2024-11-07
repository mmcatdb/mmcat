import type { SchemaCategory } from '../SchemaCategory';
import { SchemaMorphism, type SchemaMorphismFromServer } from '../SchemaMorphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CreateMorphismFromServer = SMOFromServer<SMOType.CreateMorphism> & {
    morphism: SchemaMorphismFromServer;
};

export class CreateMorphism implements SMO<SMOType.CreateMorphism> {
    readonly type = SMOType.CreateMorphism;

    private constructor(
        readonly morphism: SchemaMorphism,
    ) {}

    static fromServer(input: CreateMorphismFromServer): CreateMorphism {
        return new CreateMorphism(
            SchemaMorphism.fromServer(input.morphism),
        );
    }

    static create(morphism: SchemaMorphism): CreateMorphism {
        return new CreateMorphism(
            morphism,
        );
    }

    toServer(): CreateMorphismFromServer {
        return {
            type: SMOType.CreateMorphism,
            morphism: this.morphism.toServer(),
        };
    }

    up(category: SchemaCategory): void {
        category.getMorphism(this.morphism.signature).current = this.morphism;
    }

    down(category: SchemaCategory): void {
        category.getMorphism(this.morphism.signature).current = undefined;
    }
}
