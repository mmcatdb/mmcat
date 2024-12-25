import { ComparableMap } from '@/types/utils/ComparableMap';
import type { Key, Signature } from '../identifiers';
import type { Id } from '../id';
import { InstanceObjex, type InstanceObjexFromServer } from './InstanceObjex';
import { InstanceMorphism, type InstanceMorphismFromServer } from './InstanceMorphism';
import type { SchemaCategory } from '../schema';

export type InstanceCategoryFromServer = {
    sessionId: Id;
    categoryId: Id;
    instance: {
        objects: InstanceObjexFromServer[];
        morphisms: InstanceMorphismFromServer[];
    };
};

export class InstanceCategory {
    private constructor(
        readonly schema: SchemaCategory,
        readonly objexes: ComparableMap<Key, number, InstanceObjex>,
        readonly morphisms: ComparableMap<Signature, string, InstanceMorphism>,
    ) {}

    static fromServer(input: InstanceCategoryFromServer, schema: SchemaCategory): InstanceCategory {
        const instance = new InstanceCategory(
            schema,
            new ComparableMap(key => key.value),
            new ComparableMap(signature => signature.value),
        );

        for (const inputObjex of input.instance.objects) {
            const objex = InstanceObjex.fromServer(inputObjex, schema);
            if (objex)
                instance.objexes.set(objex.schema.key, objex);
        }

        for (const inputMorphism of input.instance.morphisms) {
            const morphism = InstanceMorphism.fromServer(inputMorphism, instance);
            if (morphism)
                instance.morphisms.set(morphism.schema.signature, morphism);
        }

        return instance;
    }
}
