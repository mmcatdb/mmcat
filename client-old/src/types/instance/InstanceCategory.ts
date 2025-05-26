import { ComparableMap } from '@/utils/ComparableMap';
import type { Key, Signature } from '../identifiers';
import type { Id } from '../id';
import { InstanceObjex, type InstanceObjexFromServer } from './InstanceObjex';
import { InstanceMorphism, type InstanceMorphismFromServer } from './InstanceMorphism';
import type { Category } from '../schema';

export type InstanceCategoryFromServer = {
    sessionId: Id;
    categoryId: Id;
    data: {
        objexes: InstanceObjexFromServer[];
        morphisms: InstanceMorphismFromServer[];
    };
};

export class InstanceCategory {
    private constructor(
        readonly schema: Category,
        readonly objexes: ComparableMap<Key, number, InstanceObjex>,
        readonly morphisms: ComparableMap<Signature, string, InstanceMorphism>,
    ) {}

    static fromServer(input: InstanceCategoryFromServer, schema: Category): InstanceCategory {
        const instance = new InstanceCategory(
            schema,
            new ComparableMap(key => key.value),
            new ComparableMap(signature => signature.value),
        );

        for (const inputObjex of input.data.objexes) {
            const objex = InstanceObjex.fromServer(inputObjex, schema);
            if (objex)
                instance.objexes.set(objex.schema.key, objex);
        }

        for (const inputMorphism of input.data.morphisms) {
            const morphism = InstanceMorphism.fromServer(inputMorphism, instance);
            if (morphism)
                instance.morphisms.set(morphism.schema.signature, morphism);
        }

        return instance;
    }
}
