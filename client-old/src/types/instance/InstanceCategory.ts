import { ComparableMap } from '@/utils/ComparableMap';
import type { Key, Signature } from '../identifiers';
import type { Id } from '../id';
import { InstanceObject, type InstanceObjectFromServer } from './InstanceObject';
import { InstanceMorphism, type InstanceMorphismFromServer } from './InstanceMorphism';
import type { SchemaCategory } from '../schema';

export type InstanceCategoryFromServer = {
    sessionId: Id;
    categoryId: Id;
    instance: {
        objects: InstanceObjectFromServer[];
        morphisms: InstanceMorphismFromServer[];
    };
};

export class InstanceCategory {
    private constructor(
        readonly schema: SchemaCategory,
        readonly objects: ComparableMap<Key, number, InstanceObject>,
        readonly morphisms: ComparableMap<Signature, string, InstanceMorphism>,
    ) {}

    static fromServer(input: InstanceCategoryFromServer, schema: SchemaCategory): InstanceCategory {
        const instance = new InstanceCategory(
            schema,
            new ComparableMap(key => key.value),
            new ComparableMap(signature => signature.value),
        );

        for (const inputObject of input.instance.objects) {
            const object = InstanceObject.fromServer(inputObject, schema);
            if (object)
                instance.objects.set(object.schema.key, object);
        }

        for (const inputMorphism of input.instance.morphisms) {
            const morphism = InstanceMorphism.fromServer(inputMorphism, instance);
            if (morphism)
                instance.morphisms.set(morphism.schema.signature, morphism);
        }

        return instance;
    }
}
