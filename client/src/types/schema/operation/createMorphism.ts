import type { Category } from '../Category';
import { type MetadataMorphism, Morphism, SchemaMorphism, type SchemaMorphismFromServer } from '../Morphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CreateMorphismFromServer = SMOFromServer<SMOType.CreateMorphism> & {
    morphism: SchemaMorphismFromServer;
};

export class CreateMorphism implements SMO<SMOType.CreateMorphism> {
    readonly type = SMOType.CreateMorphism;

    constructor(
        readonly schema: SchemaMorphism,
        readonly metadata: MetadataMorphism,
    ) {}

    static fromServer(input: CreateMorphismFromServer): CreateMorphism {
        return new CreateMorphism(
            SchemaMorphism.fromServer(input.morphism),
            null, // FIXME
        );
    }

    toServer(): CreateMorphismFromServer {
        return {
            type: SMOType.CreateMorphism,
            morphism: this.schema.toServer(),
        };
    }

    up(category: Category): void {
        const signature = this.schema.signature;
        category.morphisms.set(signature, new Morphism(signature, this.schema, this.metadata));
    }

    down(category: Category): void {
        category.morphisms.delete(this.schema.signature);
    }
}
