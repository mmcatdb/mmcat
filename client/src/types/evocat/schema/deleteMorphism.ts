import { type Category, MetadataMorphism, type MetadataMorphismResponse, Morphism, SchemaMorphism, type SchemaMorphismResponse } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type DeleteMorphismResponse = SMOResponse<SMOType.DeleteMorphism> & {
    schema: SchemaMorphismResponse;
    metadata: MetadataMorphismResponse;
};

export class DeleteMorphism implements SMO<SMOType.DeleteMorphism> {
    readonly type = SMOType.DeleteMorphism;

    constructor(
        readonly schema: SchemaMorphism,
        readonly metadata: MetadataMorphism,
    ) {}

    static fromResponse(input: DeleteMorphismResponse): DeleteMorphism {
        return new DeleteMorphism(
            SchemaMorphism.fromResponse(input.schema),
            MetadataMorphism.fromResponse(input.metadata),
        );
    }

    toServer(): DeleteMorphismResponse {
        return {
            type: SMOType.DeleteMorphism,
            schema: this.schema.toServer(),
            metadata: this.metadata.toServer(this.schema.signature),
        };
    }

    up(category: Category): void {
        category.morphisms.get(this.schema.signature)!.delete();
        category.morphisms.delete(this.schema.signature);
    }

    down(category: Category): void {
        category.morphisms.set(this.schema.signature, new Morphism(category, this.schema, this.metadata));
    }
}
