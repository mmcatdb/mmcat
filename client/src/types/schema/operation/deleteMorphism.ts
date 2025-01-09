import type { Category } from '../Category';
import { MetadataMorphism, type MetadataMorphismFromServer, Morphism, SchemaMorphism, type SchemaMorphismFromServer } from '../Morphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type DeleteMorphismFromServer = SMOFromServer<SMOType.DeleteMorphism> & {
    schema: SchemaMorphismFromServer;
    metadata: MetadataMorphismFromServer;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
    readonly type = SMOType.DeleteMorphism;

    constructor(
        readonly schema: SchemaMorphism,
        readonly metadata: MetadataMorphism,
    ) {}

    static fromServer(input: DeleteMorphismFromServer): DeleteMorphism {
        return new DeleteMorphism(
            SchemaMorphism.fromServer(input.schema),
            MetadataMorphism.fromServer(input.metadata),
        );
    }

    toServer(): DeleteMorphismFromServer {
        return {
            type: SMOType.DeleteMorphism,
            schema: this.schema.toServer(),
            metadata: this.metadata.toServer(this.schema.signature),
        };
    }

    up(category: Category): void {
        category.morphisms.delete(this.schema.signature);
    }

    down(category: Category): void {
        const from = category.getObjex(this.schema.domKey);
        const to = category.getObjex(this.schema.codKey);
        category.morphisms.set(this.schema.signature, new Morphism(category, this.schema, this.metadata, from, to));
    }
}
