import { UniqueIdProvider } from "@/utils/UniqueIdProvier";
import { Key, SchemaId, Signature } from "../identifiers";
import { SchemaMorphism, SchemaMorphismFromServer, type Max, type Min } from "./SchemaMorphism";
import { SchemaObject, type SchemaObjectFromServer } from "./SchemaObject";

export type CardinalitySettings = {
    domCodMin: Min,
    domCodMax: Max,
    codDomMin: Min,
    codDomMax: Max
}

export class SchemaCategory {
    id: number;
    jsonValue: string;
    objects: SchemaObject[];
    morphisms: SchemaMorphism[];

    _createdObjects = [] as SchemaObject[];
    _createdMorphisms = [] as SchemaMorphism[];

    _objectIdProvider = UniqueIdProvider.identity();
    _maxExistingObjectId = 0;
    _morphismIdProvider = UniqueIdProvider.identity();

    _keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    _signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private constructor(id: number, jsonValue: string, objects: SchemaObject[], morphisms: SchemaMorphism[]) {
        this.id = id;
        this.jsonValue = jsonValue;
        this.objects = objects;
        this.morphisms = morphisms;

        this.objects.forEach(object => {
            this._objectIdProvider.add(object.id);
            this._keysProvider.add(object.key);
        });
        this._maxExistingObjectId = this._objectIdProvider.maxValue;

        this.morphisms.forEach(morphism => {
            this._morphismIdProvider.add(morphism.id);
            this._signatureProvider.add(morphism.signature);
        });
    }

    static fromServer(input: SchemaCategoryFromServer): SchemaCategory {
        return new SchemaCategory(
            input.id,
            input.jsonValue,
            input.objects.map(object => SchemaObject.fromServer(object)),
            input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism))
        );
    }

    createObject(label: string, key: Key, ids: SchemaId[]): SchemaObject {
        this._keysProvider.add(key);

        const id = this._objectIdProvider.createAndAdd();
        const object = SchemaObject.createNew(id, label, key, ids);
        this._createdObjects.push(object);

        return object;
    }

    createMorphism(dom: SchemaObject, cod: SchemaObject, signature: Signature, cardinality: CardinalitySettings): { morphism: SchemaMorphism, dualMorphism: SchemaMorphism } {
        const dualSignature = signature.dual();
        this._signatureProvider.add(signature);
        this._signatureProvider.add(dualSignature);

        const id = this._morphismIdProvider.createAndAdd();
        const morphism = SchemaMorphism.createNew(id, dom.id, cod.id, signature, cardinality.domCodMin, cardinality.domCodMax);
        this._createdMorphisms.push(morphism);

        const dualId = this._morphismIdProvider.createAndAdd();
        const dualMorphism = SchemaMorphism.fromDual(dualId, morphism, dualSignature, cardinality.codDomMin, cardinality.codDomMax);
        this._createdMorphisms.push(dualMorphism);

        return { morphism, dualMorphism };
    }

    getUpdateObject() {
        return {
            objects: this._createdObjects.map(object => ({
                temporaryId: object.id,
                position: object.position,
                jsonValue: JSON.stringify(object.toJSON())
            })),
            morphisms: this._createdMorphisms.map(morphism => ({
                domId: morphism.domId <= this._maxExistingObjectId ? morphism.domId : null,
                codId: morphism.codId <= this._maxExistingObjectId ? morphism.codId : null,
                temporaryDomId: morphism.domId > this._maxExistingObjectId ? morphism.domId : null,
                temporaryCodId: morphism.codId > this._maxExistingObjectId ? morphism.codId : null,
                jsonValue: JSON.stringify(morphism.toJSON())
            }))
        };
    }

    suggestKey(): Key {
        return this._keysProvider.suggest();
    }

    isKeyAvailable(key: Key): boolean {
        return this._keysProvider.isAvailable(key);
    }

    suggestBaseSignature(): Signature {
        return this._signatureProvider.suggest();
    }

    isBaseSignatureAvailable(signature: Signature): boolean {
        return this._signatureProvider.isAvailable(signature);
    }
}

export class SchemaCategoryFromServer {
    id!: number;
    jsonValue!: string;
    objects!: SchemaObjectFromServer[];
    morphisms!: SchemaMorphismFromServer[];
}
