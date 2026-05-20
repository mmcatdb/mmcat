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
        category.morphisms.set(this.schema.signature, new Morphism(category, this.schema, this.metadata));
    }

    down(category: Category): void {
        category.morphisms.get(this.schema.signature)!.delete();
        category.morphisms.delete(this.schema.signature);
    }
}
