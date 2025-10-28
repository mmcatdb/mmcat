import { MetadataObjex, type MetadataObjexResponse, type Category } from '@/types/schema';
import { type MMO, type MMOResponse, MMOType } from './mmo';
import { Key } from '@/types/identifiers';

export type ObjexMetadataResponse = MMOResponse<MMOType.Objex> & {
    newObjex?: MetadataObjexResponse;
    oldObjex?: MetadataObjexResponse;
};

export class ObjexMetadata implements MMO<MMOType.Objex> {
    readonly type = MMOType.Objex;

    private constructor(
        readonly key: Key,
        readonly newObjex?: MetadataObjex,
        readonly oldObjex?: MetadataObjex,
    ) {}

    static fromResponse(input: ObjexMetadataResponse): ObjexMetadata {
        const keyResponse = input.newObjex?.key ?? input.oldObjex?.key;
        if (!keyResponse)
            throw new Error('ObjexMetadata must have at least one objex.');

        return new ObjexMetadata(
            Key.fromResponse(keyResponse),
            input.newObjex && MetadataObjex.fromResponse(input.newObjex),
            input.oldObjex && MetadataObjex.fromResponse(input.oldObjex),
        );
    }

    static create(key: Key, newObjex?: MetadataObjex, oldObjex?: MetadataObjex): ObjexMetadata {
        return new ObjexMetadata(
            key,
            newObjex,
            oldObjex,
        );
    }

    toServer(): ObjexMetadataResponse {
        return {
            type: MMOType.Objex,
            newObjex: this.newObjex?.toServer(this.key),
            oldObjex: this.oldObjex?.toServer(this.key),
        };
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    up(category: Category): void {
        // category.getObjex(this.key).current = this.newObjex;
        // TODO do something
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    down(category: Category): void {
        // category.getObjex(this.key).current = this.oldObjex;
        // TODO do something
    }
}
