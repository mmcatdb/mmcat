import { type Category, MetadataMorphism, type MetadataMorphismResponse, Morphism, SchemaMorphism, type SchemaMorphismResponse } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type CreateMorphismResponse = SMOResponse<SMOType.CreateMorphism> & {
    schema: SchemaMorphismResponse;
    metadata: MetadataMorphismResponse;
};

export class CreateMorphism implements SMO<SMOType.CreateMorphism> {
    readonly type = SMOType.CreateMorphism;

    constructor(
        readonly schema: SchemaMorphism,
        readonly metadata: MetadataMorphism,
    ) {}

    static fromResponse(input: CreateMorphismResponse): CreateMorphism {
        return new CreateMorphism(
            SchemaMorphism.fromResponse(input.schema),
            MetadataMorphism.fromResponse(input.metadata),
        );
    }

    toServer(): CreateMorphismResponse {
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
