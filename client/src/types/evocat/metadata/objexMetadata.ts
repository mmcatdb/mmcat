import { MetadataObjex, type MetadataObjexFromServer, type Category } from '@/types/schema';
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
        readonly newObjex?: MetadataObjex,
        readonly oldObjex?: MetadataObjex,
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
            newObject: this.newObjex?.toServer(this.key),
            oldObject: this.oldObjex?.toServer(this.key),
        };
    }

    up(category: Category): void {
        // category.getObject(this.key).current = this.newObject;
        console.log('up', category);
    }

    down(category: Category): void {
        // category.getObject(this.key).current = this.oldObject;
        console.log('down', category);
    }
}
