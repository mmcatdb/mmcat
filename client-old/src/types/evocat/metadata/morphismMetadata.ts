import { MetadataMorphism, type MetadataMorphismFromServer, Category } from '@/types/schema';
import { type MMO, type MMOFromServer, MMOType } from './mmo';
import { Signature } from '@/types/identifiers';

export type MorphismMetadataFromServer = MMOFromServer<MMOType.Morphism> & {
    newMorphism?: MetadataMorphismFromServer;
    oldMorphism?: MetadataMorphismFromServer;
};

export class MorphismMetadata implements MMO<MMOType.Morphism> {
    readonly type = MMOType.Morphism;

    private constructor(
        readonly signature: Signature,
        readonly newMorphism?: MetadataMorphism,
        readonly oldMorphism?: MetadataMorphism,
    ) {}

    static fromServer(input: MorphismMetadataFromServer): MorphismMetadata {
        const signatureFromServer = input.newMorphism?.signature ?? input.oldMorphism?.signature;
        if (!signatureFromServer)
            throw new Error('MorphismMetadata must have at least one morphism.');

        return new MorphismMetadata(
            Signature.fromServer(signatureFromServer),
            input.newMorphism && MetadataMorphism.fromServer(input.newMorphism),
            input.oldMorphism && MetadataMorphism.fromServer(input.oldMorphism),
        );
    }

    static create(signature: Signature, newMorphism?: MetadataMorphism, oldMorphism?: MetadataMorphism): MorphismMetadata {
        return new MorphismMetadata(
            signature,
            newMorphism,
            oldMorphism,
        );
    }

    toServer(): MorphismMetadataFromServer {
        return {
            type: MMOType.Morphism,
            newMorphism: this.newMorphism?.toServer(this.signature),
            oldMorphism: this.oldMorphism?.toServer(this.signature),
        };
    }

    up(category: Category): void {
        // category.getMorphism(this.signature).current = this.newMorphism;
    }

    down(category: Category): void {
        // category.getMorphism(this.signature).current = this.oldMorphism;
    }
}
