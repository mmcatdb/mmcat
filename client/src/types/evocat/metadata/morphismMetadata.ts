import { MetadataMorphism, type MetadataMorphismResponse, type Category } from '@/types/schema';
import { type MMO, type MMOResponse, MMOType } from './mmo';
import { Signature } from '@/types/identifiers';

export type MorphismMetadataResponse = MMOResponse<MMOType.Morphism> & {
    newMorphism?: MetadataMorphismResponse;
    oldMorphism?: MetadataMorphismResponse;
};

export class MorphismMetadata implements MMO<MMOType.Morphism> {
    readonly type = MMOType.Morphism;

    private constructor(
        readonly signature: Signature,
        readonly newMorphism?: MetadataMorphism,
        readonly oldMorphism?: MetadataMorphism,
    ) {}

    static fromResponse(input: MorphismMetadataResponse): MorphismMetadata {
        const signatureResponse = input.newMorphism?.signature ?? input.oldMorphism?.signature;
        if (!signatureResponse)
            throw new Error('MorphismMetadata must have at least one morphism.');

        return new MorphismMetadata(
            Signature.fromResponse(signatureResponse),
            input.newMorphism && MetadataMorphism.fromResponse(input.newMorphism),
            input.oldMorphism && MetadataMorphism.fromResponse(input.oldMorphism),
        );
    }

    static create(signature: Signature, newMorphism?: MetadataMorphism, oldMorphism?: MetadataMorphism): MorphismMetadata {
        return new MorphismMetadata(
            signature,
            newMorphism,
            oldMorphism,
        );
    }

    toServer(): MorphismMetadataResponse {
        return {
            type: MMOType.Morphism,
            newMorphism: this.newMorphism?.toServer(this.signature),
            oldMorphism: this.oldMorphism?.toServer(this.signature),
        };
    }

    up(category: Category): void {
        // category.getMorphism(this.signature).current = this.newMorphism;
        console.log('up', category);
    }

    down(category: Category): void {
        // category.getMorphism(this.signature).current = this.oldMorphism;
        console.log('down', category);
    }
}
