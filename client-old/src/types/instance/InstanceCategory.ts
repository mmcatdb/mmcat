import { ComparableMap } from '@/utils/ComparableMap';
import type { Key, Signature } from '../identifiers';
import type { Id } from '../id';
import { InstanceObject, type InstanceObjectFromServer } from './InstanceObject';
import { InstanceMorphism, type InstanceMorphismFromServer } from './InstanceMorphism';
import type { Category } from '../schema';

export type InstanceCategoryFromServer = {
    sessionId: Id;
    categoryId: Id;
    data: {
        objects: InstanceObjectFromServer[];
        morphisms: InstanceMorphismFromServer[];
    };
};

export class InstanceCategory {
    private constructor(
        readonly schema: Category,
        readonly objects: ComparableMap<Key, number, InstanceObject>,
        readonly morphisms: ComparableMap<Signature, string, InstanceMorphism>,
    ) {}

    static fromServer(input: InstanceCategoryFromServer, schema: Category): InstanceCategory {
        const instance = new InstanceCategory(
            schema,
            new ComparableMap(key => key.value),
            new ComparableMap(signature => signature.value),
        );

        for (const inputObject of input.data.objects) {
            const object = InstanceObject.fromServer(inputObject, schema);
            if (object)
                instance.objects.set(object.schema.key, object);
        }

        for (const inputMorphism of input.data.morphisms) {
            const morphism = InstanceMorphism.fromServer(inputMorphism, instance);
            if (morphism)
                instance.morphisms.set(morphism.schema.signature, morphism);
        }

        return instance;
    }
}
