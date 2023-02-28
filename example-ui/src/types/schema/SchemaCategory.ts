import type { Iri } from "@/types/integration";
import { UniqueIdProvider } from "@/utils/UniqueIdProvier";
import { ComplexProperty, type ParentProperty } from "@/types/accessPath/basic";
import type { Entity, Id, Version } from "../id";
import { DynamicName, Key, Signature, ObjectIds } from "../identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Mapping } from "../mapping";
import { SchemaMorphism, type SchemaMorphismFromServer, Tag, type Max, type Min } from "./SchemaMorphism";
import { SchemaObject, type SchemaObjectFromServer } from "./SchemaObject";
import { SchemaCategoryEvolver, type SchemaCategoryUpdate } from "./SchemaCategoryUpdate";

export type CardinalitySettings = {
    domCodMin: Min;
    domCodMax: Max;
    codDomMin: Min;
    codDomMax: Max;
};

export function compareCardinalitySettings(settings1: CardinalitySettings, settings2: CardinalitySettings): boolean {
    return settings1.domCodMin === settings2.domCodMin &&
        settings1.domCodMax === settings2.domCodMax &&
        settings1.codDomMin === settings2.codDomMin &&
        settings1.codDomMax === settings2.codDomMax;
}

export class SchemaCategory implements Entity {
    readonly id: Id;
    readonly label: string;
    readonly version: Version;
    objects: SchemaObject[];
    morphisms: SchemaMorphism[];
    notAvailableIris = new Set as Set<Iri>;

    readonly evolver = new SchemaCategoryEvolver();

    _keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    _signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private constructor(id: Id, label: string, version: Version, objects: SchemaObject[], morphisms: SchemaMorphism[]) {
        this.id = id;
        this.label = label;
        this.version = version;
        this.objects = objects;
        this.morphisms = morphisms;

        this.objects.forEach(object => {
            this._keysProvider.add(object.key);
            if (object.iri)
                this.notAvailableIris.add(object.iri);
        });

        this.morphisms.forEach(morphism => {
            this._signatureProvider.add(morphism.signature);
            if (morphism.iri)
                this.notAvailableIris.add(morphism.iri);
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
            input.label,
            input.version,
            input.objects.map(SchemaObject.fromServer),
            morphisms
        );
    }

    _createObjectWithoutCheck(label: string, ids?: ObjectIds, iri?: Iri, pimIri?: Iri): SchemaObject {
        const key = this._keysProvider.createAndAdd();
        const object = SchemaObject.createNew(key, label, ids, iri, pimIri);
        this.objects.push(object);
        this.evolver.addObject(object);

        return object;
    }

    createObject(label: string, ids?: ObjectIds): SchemaObject {
        return this._createObjectWithoutCheck(label, ids);
    }

    createObjectWithIri(label: string, ids: ObjectIds | undefined, iri: Iri, pimIri: Iri): SchemaObject | null {
        if (!this.iriIsAvailable(iri)) {
            console.log('Object with iri ' + iri + " already exists.");
            return null;
        }

        this.notAvailableIris.add(iri);

        return this._createObjectWithoutCheck(label, ids, iri, pimIri);
    }

    findObjectByIri(iri: Iri): SchemaObject | undefined {
        return this.objects.find(object => object.iri === iri);
    }

    createMorphismWithDual(dom: SchemaObject, cod: SchemaObject, cardinality: CardinalitySettings, label: string, tags: Tag[] = []): SchemaMorphism {
        const signature = this._signatureProvider.createAndAdd();
        const dualSignature = signature.dual();
        this._signatureProvider.add(dualSignature);

        const morphism = SchemaMorphism.createNew(signature, dom.key, cod.key, cardinality.domCodMin, cardinality.domCodMax, label, tags);
        this.morphisms.push(morphism);
        this.evolver.addMorphism(morphism);

        const dualMorphism = SchemaMorphism.createNewFromDualWithoutTags(morphism, dualSignature, cardinality.codDomMin, cardinality.codDomMax);
        this.morphisms.push(dualMorphism);
        this.evolver.addMorphism(dualMorphism);

        morphism.dual = dualMorphism;
        dualMorphism.dual = morphism;

        return morphism;
    }

    iriIsAvailable(iri: Iri): boolean {
        return !this.notAvailableIris.has(iri);
    }

    createMorphismWithDualWithIri(dom: SchemaObject, cod: SchemaObject, cardinality: CardinalitySettings, iri: Iri, pimIri: Iri, label: string, tags: Tag[] = []): SchemaMorphism | null {
        if (!this.iriIsAvailable(iri)) {
            console.log('Morphism with iri ' + iri + " already exists.");
            return null;
        }

        this.notAvailableIris.add(iri);
        const newMorphism = this.createMorphismWithDual(dom, cod, cardinality, label, tags);
        newMorphism.iri = iri;
        newMorphism.pimIri = pimIri;

        return newMorphism;
    }

    editMorphismWithDual(morphism: SchemaMorphism, dom: SchemaObject, cod: SchemaObject, cardinality: CardinalitySettings, label: string) {
        morphism.update(dom.key, cod.key, cardinality.domCodMin, cardinality.domCodMax, label);
        morphism.dual.update(cod.key, dom.key, cardinality.codDomMin, cardinality.codDomMax, label);
    }

    deleteObject(object: SchemaObject) {
        this.evolver.deleteObject(object);
    }

    deleteMorphismWithDual(morphism: SchemaMorphism) {
        this.evolver.deleteMorphismWithDual(morphism);
    }

    getUpdateObject(): SchemaCategoryUpdate {
        return {
            beforeVersion: this.version,
            operations: this.evolver.generateOperations()
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

    setDatabaseToObjectsFromMapping(mapping: Mapping, logicalModel: LogicalModel): void {
        const objects = getObjectsFromPath(mapping.accessPath, this.objects, this.morphisms);

        const rootObject = this.objects.find(object => object.equals(mapping.rootObject));
        if (rootObject)
            objects.push(rootObject);

        objects.forEach(object => object.setLogicalModel(logicalModel));
    }
}

export type SchemaCategoryFromServer = {
    id: Id;
    label: string;
    version: Version;
    objects: SchemaObjectFromServer[];
    morphisms: SchemaMorphismFromServer[];
};

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

    return objects.find(object => object.key.equals(morphism.codKey));
}

export type SchemaCategoryInfoFromServer = {
    id: Id;
    label: string;
    version: Version;
};

export class SchemaCategoryInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly version: Version
    ) {}

    static fromServer(input: SchemaCategoryInfoFromServer): SchemaCategoryInfo {
        return new SchemaCategoryInfo(
            input.id,
            input.label,
            input.version
        );
    }
}

export type SchemaCategoryInit = {
    label: string;
};
