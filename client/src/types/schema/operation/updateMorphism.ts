import type { Category } from '../Category';
import { SchemaMorphism, type SchemaMorphismFromServer } from '../Morphism';
import { type SMO, type SMOFromServer, SMOType } from './smo';

export type UpdateMorphismFromServer = SMOFromServer<SMOType.UpdateMorphism> & {
    newMorphism: SchemaMorphismFromServer;
    oldMorphism: SchemaMorphismFromServer;
};

export class UpdateMorphism implements SMO<SMOType.UpdateMorphism> {
    readonly type = SMOType.UpdateMorphism;

    private constructor(
        readonly newMorphism: SchemaMorphism,
        readonly oldMorphism: SchemaMorphism,
    ) {}

    static fromServer(input: UpdateMorphismFromServer): UpdateMorphism {
        return new UpdateMorphism(
            SchemaMorphism.fromServer(input.newMorphism),
            SchemaMorphism.fromServer(input.oldMorphism),
        );
    }

    static create(newMorphism: SchemaMorphism, oldMorphism: SchemaMorphism): UpdateMorphism {
        if (!newMorphism.signature.equals(oldMorphism.signature))
            throw new Error('Cannot edit morphism\'s signature.');

        return new UpdateMorphism(
            newMorphism,
            oldMorphism,
        );
    }

    toServer(): UpdateMorphismFromServer {
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
