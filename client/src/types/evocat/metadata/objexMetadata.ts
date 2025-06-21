import { MetadataObjex, type MetadataObjexFromServer, type Category } from '@/types/schema';
import { type MMO, type MMOFromServer, MMOType } from './mmo';
import { Key } from '@/types/identifiers';

export type ObjexMetadataFromServer = MMOFromServer<MMOType.Objex> & {
    newObjex?: MetadataObjexFromServer;
    oldObjex?: MetadataObjexFromServer;
};

export class ObjexMetadata implements MMO<MMOType.Objex> {
    readonly type = MMOType.Objex;

    private constructor(
        readonly key: Key,
        readonly newObjex?: MetadataObjex,
        readonly oldObjex?: MetadataObjex,
    ) {}

    static fromServer(input: ObjexMetadataFromServer): ObjexMetadata {
        const keyFromServer = input.newObjex?.key ?? input.oldObjex?.key;
        if (!keyFromServer)
            throw new Error('ObjexMetadata must have at least one objex.');

        return new ObjexMetadata(
            Key.fromServer(keyFromServer),
            input.newObjex && MetadataObjex.fromServer(input.newObjex),
            input.oldObjex && MetadataObjex.fromServer(input.oldObjex),
        );
    }

    static create(key: Key, newObjex?: MetadataObjex, oldObjex?: MetadataObjex): ObjexMetadata {
        return new ObjexMetadata(
            key,
            newObjex,
            oldObjex,
        );
    }

    toServer(): ObjexMetadataFromServer {
        return {
            type: MMOType.Objex,
            newObjex: this.newObjex?.toServer(this.key),
            oldObjex: this.oldObjex?.toServer(this.key),
        };
    }

    up(category: Category): void {
        // category.getObjex(this.key).current = this.newObjex;
    }

    down(category: Category): void {
        // category.getObjex(this.key).current = this.oldObjex;
    }
}
