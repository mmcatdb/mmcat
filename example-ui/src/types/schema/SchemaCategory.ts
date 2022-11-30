import { UniqueIdProvider } from "@/utils/UniqueIdProvier";
import { ComplexProperty, type ParentProperty } from "../accessPath/basic";
import { DynamicName, Key, SchemaId, Signature } from "../identifiers";
import type { Mapping } from "../mapping";
import { SchemaMorphism, SchemaMorphismFromServer, type Max, type Min } from "./SchemaMorphism";
import { SchemaObject, type SchemaObjectFromServer } from "./SchemaObject";

export type CardinalitySettings = {
    domCodMin: Min,
    domCodMax: Max,
    codDomMin: Min,
    codDomMax: Max
}

export function compareCardinalitySettings(settings1: CardinalitySettings, settings2: CardinalitySettings): boolean {
    return settings1.domCodMin === settings2.domCodMin &&
        settings1.domCodMax === settings2.domCodMax &&
        settings1.codDomMin === settings2.codDomMin &&
        settings1.codDomMax === settings2.codDomMax;
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
        const morphisms = input.morphisms.map(SchemaMorphism.fromServer);
        morphisms.forEach(morphism => {
            const dualSignature = morphism.signature.dual();
            const dualMorphism = morphisms.find(otherMorphism => otherMorphism.signature.equals(dualSignature));
            if (dualMorphism)
                morphism.dual = dualMorphism;
        });

        return new SchemaCategory(
            input.id,
            input.jsonValue,
            input.objects.map(SchemaObject.fromServer),
            morphisms
        );
    }

    createObject(label: string, ids: SchemaId[]): SchemaObject {
        const key = this._keysProvider.createAndAdd();
        const id = this._objectIdProvider.createAndAdd();
        const object = SchemaObject.createNew(id, label, key, ids);
        this._createdObjects.push(object);

        return object;
    }

    createMorphismWithDual(dom: SchemaObject, cod: SchemaObject, cardinality: CardinalitySettings): SchemaMorphism {
        const signature = this._signatureProvider.createAndAdd();
        const dualSignature = signature.dual();
        this._signatureProvider.add(dualSignature);

        const id = this._morphismIdProvider.createAndAdd();
        const morphism = SchemaMorphism.createNew(id, dom.id, cod.id, signature, cardinality.domCodMin, cardinality.domCodMax);
        this._createdMorphisms.push(morphism);

        const dualId = this._morphismIdProvider.createAndAdd();
        const dualMorphism = SchemaMorphism.createNewFromDual(dualId, morphism, dualSignature, cardinality.codDomMin, cardinality.codDomMax);
        this._createdMorphisms.push(dualMorphism);

        morphism.dual = dualMorphism;
        dualMorphism.dual = morphism;

        return morphism;
    }

    editMorphismWithDual(morphism: SchemaMorphism, dom: SchemaObject, cod: SchemaObject, cardinality: CardinalitySettings) {
        morphism.update(dom.id, cod.id, cardinality.domCodMin, cardinality.domCodMax);
        morphism.dual.update(cod.id, dom.id, cardinality.codDomMin, cardinality.codDomMax);
    }

    deleteObject(object: SchemaObject) {
        this._createdObjects = this._createdObjects.filter(o => o.id !== object.id);
    }

    deleteMorphismWithDual(morphism: SchemaMorphism) {
        this._createdMorphisms = this._createdMorphisms.filter(m => m.id !== morphism.id && m.id !== morphism.dual.id);
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

    setDatabaseToObjectsFromMapping(mapping: Mapping): void {
        const objects = getObjectsFromPath(mapping.accessPath, this.objects, this.morphisms);

        const rootObject = this.objects.find(object => object.id === mapping.rootObjectId);
        if (rootObject)
            objects.push(rootObject);

        objects.forEach(object => object.setDatabase(mapping.logicalModel.databaseView));
    }
}

export class SchemaCategoryFromServer {
    id!: number;
    jsonValue!: string;
    objects!: SchemaObjectFromServer[];
    morphisms!: SchemaMorphismFromServer[];
}

function getObjectsFromPath(path: ParentProperty, objects: SchemaObject[], morphisms: SchemaMorphism[]): SchemaObject[] {
    const output = [] as SchemaObject[];

    path.subpaths.forEach(subpath => {
        const subpathObject = findObjectFromSignature(subpath.signature, objects, morphisms);
        if (subpathObject)
            output.push(subpathObject);

        if (subpath.name instanceof DynamicName) {
            const nameObject = findObjectFromSignature(subpath.name.signature, objects, morphisms);
            if (nameObject)
                output.push(nameObject);
        }

        if (subpath instanceof ComplexProperty)
            output.push(...getObjectsFromPath(subpath, objects, morphisms));
    });

    return output;
}

function findObjectFromSignature(signature: Signature, objects: SchemaObject[], morphisms: SchemaMorphism[]): SchemaObject | undefined {
    const base = signature.getLastBase();
    if (!base)
        return undefined;

    const morphism = morphisms.find(morphism => morphism.signature.equals(base.last));
    if (!morphism)
        return undefined;

    return objects.find(object => object.id === morphism.codId);
}

export class SchemaCategoryInfo {
    private constructor(public id: number, public name: string) {}

    static fromServer(input: SchemaCategoryInfoFromServer): SchemaCategoryInfo {
        const parsed = JSON.parse(input.jsonValue) as { name: string };
        return new SchemaCategoryInfo(input.id, parsed.name);
    }
}

export type SchemaCategoryInfoFromServer = {
    id: number;
    jsonValue: string;
}
