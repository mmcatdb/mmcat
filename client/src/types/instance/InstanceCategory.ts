import { ComparableMap } from '@/types/utils/ComparableMap';
import type { Key, Signature } from '../identifiers';
import type { Id } from '../id';
import { InstanceObjex, type InstanceObjexResponse } from './InstanceObjex';
import { InstanceMorphism, type InstanceMorphismResponse } from './InstanceMorphism';
import type { Category } from '../schema';

export type InstanceCategoryResponse = {
    sessionId: Id;
    categoryId: Id;
    instance: {
        objexes: InstanceObjexResponse[];
        morphisms: InstanceMorphismResponse[];
    };
};

export class InstanceCategory {
    private constructor(
        readonly schema: Category,
        readonly objexes: ComparableMap<Key, number, InstanceObjex>,
        readonly morphisms: ComparableMap<Signature, string, InstanceMorphism>,
    ) {}

    static fromResponse(input: InstanceCategoryResponse, schema: Category): InstanceCategory {
        const instance = new InstanceCategory(
            schema,
            new ComparableMap(key => key.value),
            new ComparableMap(signature => signature.value),
        );

        for (const inputObjex of input.instance.objexes) {
            const objex = InstanceObjex.fromResponse(inputObjex, schema);
            if (objex)
                instance.objexes.set(objex.schema.key, objex);
        }

        for (const inputMorphism of input.instance.morphisms) {
            const morphism = InstanceMorphism.fromResponse(inputMorphism, instance);
            if (morphism)
                instance.morphisms.set(morphism.schema.signature, morphism);
        }

        return instance;
    }
}
