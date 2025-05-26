import { MetadataObjex, type MetadataObjexFromServer, Category } from '@/types/schema';
import { type MMO, type MMOFromServer, MMOType } from './mmo';
import { Key } from '@/types/identifiers';

export type ObjexMetadataFromServer = MMOFromServer<MMOType.Objex> & {
    newObject?: MetadataObjexFromServer;
    oldObject?: MetadataObjexFromServer;
};

export class ObjexMetadata implements MMO<MMOType.Objex> {
    readonly type = MMOType.Objex;

    private constructor(
        readonly key: Key,
        readonly newObject?: MetadataObjex,
        readonly oldObject?: MetadataObjex,
    ) {}

    static fromServer(input: ObjexMetadataFromServer): ObjexMetadata {
        const keyFromServer = input.newObject?.key ?? input.oldObject?.key;
        if (!keyFromServer)
            throw new Error('ObjectMetadata must have at least one object.');

        return new ObjexMetadata(
            Key.fromServer(keyFromServer),
            input.newObject && MetadataObjex.fromServer(input.newObject),
            input.oldObject && MetadataObjex.fromServer(input.oldObject),
        );
    }

    static create(key: Key, newObject?: MetadataObjex, oldObject?: MetadataObjex): ObjexMetadata {
        return new ObjexMetadata(
            key,
            newObject,
            oldObject,
        );
    }

    toServer(): ObjexMetadataFromServer {
        return {
            type: MMOType.Objex,
            newObject: this.newObject?.toServer(this.key),
            oldObject: this.oldObject?.toServer(this.key),
        };
    }

    up(category: Category): void {
        // category.getObject(this.key).current = this.newObject;
    }

    down(category: Category): void {
        // category.getObject(this.key).current = this.oldObject;
    }
}
