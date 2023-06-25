import type { SchemaCategory } from '../SchemaCategory';
import { SchemaMorphism, type SchemaMorphismFromServer } from '../SchemaMorphism';
import { type SMO, type SMOFromServer, SMOType } from './schemaModificationOperation';

export type EditMorphismFromServer = SMOFromServer<SMOType.EditMorphism> & {
    newMorphism: SchemaMorphismFromServer;
    oldMorphism: SchemaMorphismFromServer;
};

export class EditMorphism implements SMO<SMOType.EditMorphism> {
    readonly type = SMOType.EditMorphism;

    private constructor(
        readonly newMorphism: SchemaMorphism,
        readonly oldMorphism: SchemaMorphism,
    ) {}

    static fromServer(input: EditMorphismFromServer): EditMorphism {
        return new EditMorphism(
            SchemaMorphism.fromServer(input.newMorphism),
            SchemaMorphism.fromServer(input.oldMorphism),
        );
    }

    static create(newMorphism: SchemaMorphism, oldMorphism: SchemaMorphism): EditMorphism {
        if (!newMorphism.signature.equals(oldMorphism.signature))
            throw new Error('Cannot edit morphism\'s signature.');

        return new EditMorphism(
            newMorphism,
            oldMorphism,
        );
    }

    toServer(): EditMorphismFromServer {
        return {
            type: SMOType.EditMorphism,
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
