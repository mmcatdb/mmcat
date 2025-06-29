import { type Category, SchemaMorphism, type SchemaMorphismResponse } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type UpdateMorphismResponse = SMOResponse<SMOType.UpdateMorphism> & {
    newMorphism: SchemaMorphismResponse;
    oldMorphism: SchemaMorphismResponse;
};

export class UpdateMorphism implements SMO<SMOType.UpdateMorphism> {
    readonly type = SMOType.UpdateMorphism;

    private constructor(
        readonly newMorphism: SchemaMorphism,
        readonly oldMorphism: SchemaMorphism,
    ) {}

    static fromResponse(input: UpdateMorphismResponse): UpdateMorphism {
        return new UpdateMorphism(
            SchemaMorphism.fromResponse(input.newMorphism),
            SchemaMorphism.fromResponse(input.oldMorphism),
        );
    }

    static create(newMorphism: SchemaMorphism, oldMorphism: SchemaMorphism): UpdateMorphism {
        if (!newMorphism.signature.equals(oldMorphism.signature))
            throw new Error('Cannot update morphism\'s signature.');

        return new UpdateMorphism(
            newMorphism,
            oldMorphism,
        );
    }

    toServer(): UpdateMorphismResponse {
        return {
            type: SMOType.UpdateMorphism,
            newMorphism: this.newMorphism.toServer(),
            oldMorphism: this.oldMorphism.toServer(),
        };
    }

    up(category: Category): void {
        category.getMorphism(this.newMorphism.signature).schema = this.newMorphism;
    }

    down(category: Category): void {
        category.getMorphism(this.oldMorphism.signature).schema = this.oldMorphism;
    }
}
