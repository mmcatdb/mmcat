import type { Category } from '../Category';
import { MetadataMorphism, type MetadataMorphismFromServer, Morphism, SchemaMorphism, type SchemaMorphismFromServer } from '../Morphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type CreateMorphismFromServer = SMOFromServer<SMOType.CreateMorphism> & {
    schema: SchemaMorphismFromServer;
    metadata: MetadataMorphismFromServer;
};

export class CreateMorphism implements SMO<SMOType.CreateMorphism> {
    readonly type = SMOType.CreateMorphism;

    constructor(
        readonly schema: SchemaMorphism,
        readonly metadata: MetadataMorphism,
    ) {}

    static fromServer(input: CreateMorphismFromServer): CreateMorphism {
        return new CreateMorphism(
            SchemaMorphism.fromServer(input.schema),
            MetadataMorphism.fromServer(input.metadata),
        );
    }

    toServer(): CreateMorphismFromServer {
        return {
            type: SMOType.CreateMorphism,
            schema: this.schema.toServer(),
            metadata: this.metadata.toServer(this.schema.signature),
        };
    }

    up(category: Category): void {
        const from = category.getObjex(this.schema.domKey);
        const to = category.getObjex(this.schema.codKey);
        category.morphisms.set(this.schema.signature, new Morphism(category, this.schema, this.metadata, from, to));
    }

    down(category: Category): void {
        category.morphisms.delete(this.schema.signature);
    }
}
