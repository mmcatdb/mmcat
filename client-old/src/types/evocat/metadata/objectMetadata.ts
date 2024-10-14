import { MetadataObject, type MetadataObjectFromServer, SchemaCategory } from '@/types/schema';
import { type MMO, type MMOFromServer, MMOType } from './mmo';
import { Key } from '@/types/identifiers';

export type ObjectMetadataFromServer = MMOFromServer<MMOType.Object> & {
    newObject?: MetadataObjectFromServer;
    oldObject?: MetadataObjectFromServer;
};

export class ObjectMetadata implements MMO<MMOType.Object> {
    readonly type = MMOType.Object;

    private constructor(
        readonly key: Key,
        readonly newObject?: MetadataObject,
        readonly oldObject?: MetadataObject,
    ) {}

    static fromServer(input: ObjectMetadataFromServer): ObjectMetadata {
        const keyFromServer = input.newObject?.key ?? input.oldObject?.key;
        if (!keyFromServer)
            throw new Error('ObjectMetadata must have at least one object.');

        return new ObjectMetadata(
            Key.fromServer(keyFromServer),
            input.newObject && MetadataObject.fromServer(input.newObject),
            input.oldObject && MetadataObject.fromServer(input.oldObject),
        );
    }

    static create(key: Key, newObject?: MetadataObject, oldObject?: MetadataObject): ObjectMetadata {
        return new ObjectMetadata(
            key,
            newObject,
            oldObject,
        );
    }

    toServer(): ObjectMetadataFromServer {
        return {
            type: MMOType.Object,
            newObject: this.newObject?.toServer(this.key),
            oldObject: this.oldObject?.toServer(this.key),
        };
    }

    up(category: SchemaCategory): void {
        // category.getObject(this.key).current = this.newObject;
    }

    down(category: SchemaCategory): void {
        // category.getObject(this.key).current = this.oldObject;
    }
}