import type { Category } from '../Category';
import { type MetadataMorphism, Morphism, SchemaMorphism, type SchemaMorphismFromServer } from '../Morphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
    morphism: SchemaMorphismFromServer;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
    readonly type = SMOType.DeleteMorphism;

    constructor(
        readonly schema: SchemaMorphism,
        readonly metadata: MetadataMorphism,
    ) {}

    static fromServer(input: DeleteMorphismFromServer): DeleteMorphism {
        return new DeleteMorphism(
            SchemaMorphism.fromServer(input.morphism),
            null, // FIXME
        );
    }

    toServer(): DeleteMorphismFromServer {
        return {
            type: SMOType.DeleteMorphism,
            morphism: this.schema.toServer(),
        };
    }

    up(category: Category): void {
        category.morphisms.delete(this.schema.signature);
    }

    down(category: Category): void {
        const signature = this.schema.signature;
        category.morphisms.set(signature, new Morphism(signature, this.schema, this.metadata));
    }
}
