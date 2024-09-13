import type { SchemaCategory } from '../SchemaCategory';
import { SchemaMorphism, type SchemaMorphismFromServer } from '../SchemaMorphism';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

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

    up(category: SchemaCategory): void {
        category.getMorphism(this.newMorphism.signature).current = this.newMorphism;
    }

    down(category: SchemaCategory): void {
        category.getMorphism(this.oldMorphism.signature).current = this.oldMorphism;
    }
}
