import { type Category, SchemaObjex, type SchemaObjexResponse } from '@/types/schema';
import { type SMO, type SMOResponse, SMOType } from './smo';

export type UpdateObjexResponse = SMOResponse<SMOType.UpdateObjex> & {
    newObjex: SchemaObjexResponse;
    oldObjex: SchemaObjexResponse;
};

export class UpdateObjex implements SMO<SMOType.UpdateObjex> {
    readonly type = SMOType.UpdateObjex;

    private constructor(
        readonly newObjex: SchemaObjex,
        readonly oldObjex: SchemaObjex,
    ) {}

    static fromResponse(input: UpdateObjexResponse): UpdateObjex {
        return new UpdateObjex(
            SchemaObjex.fromResponse(input.newObjex),
            SchemaObjex.fromResponse(input.oldObjex),
        );
    }

    static create(newObjex: SchemaObjex, oldObjex: SchemaObjex): UpdateObjex {
        if (!newObjex.key.equals(oldObjex.key))
            throw new Error('Cannot update objex\'s key.');

        return new UpdateObjex(
            newObjex,
            oldObjex,
        );
    }

    toServer(): UpdateObjexResponse {
        return {
            type: SMOType.UpdateObjex,
            newObjex: this.newObjex.toServer(),
            oldObjex: this.oldObjex.toServer(),
        };
    }

    up(category: Category): void {
        category.getObjex(this.newObjex.key).schema = this.newObjex;
    }

    down(category: Category): void {
        category.getObjex(this.oldObjex.key).schema = this.oldObjex;
    }
}
