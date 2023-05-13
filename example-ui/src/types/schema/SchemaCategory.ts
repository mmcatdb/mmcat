import type { Iri } from "@/types/integration";
import { UniqueIdProvider } from "@/utils/UniqueIdProvider";
import { ComplexProperty, type ParentProperty } from "@/types/accessPath/basic";
import type { Entity, Id, Version } from "../id";
import { DynamicName, Key, Signature } from "../identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Mapping } from "../mapping";
import { SchemaMorphism, type SchemaMorphismFromServer, type Min, type MorphismDefinition } from "./SchemaMorphism";
import { SchemaObject, type ObjectDefinition, type SchemaObjectFromServer } from "./SchemaObject";
import { SchemaCategoryEvolver } from "./SchemaCategoryUpdate";
import type { SMOFromServer } from "./SchemaModificationOperation";

export class SchemaCategory implements Entity {
    readonly id: Id;
    readonly label: string;
    readonly version: Version;
    objects: SchemaObject[];
    morphisms: SchemaMorphism[];
    notAvailableIris: Set<Iri> = new Set;

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

        return new SchemaCategory(
            input.id,
            input.label,
            input.version,
            input.objects.map(SchemaObject.fromServer),
            morphisms,
        );
    }

    createObject(def: ObjectDefinition): SchemaObject {
        if ('iri' in def)
            this.notAvailableIris.add(def.iri);

        const key = this._keysProvider.createAndAdd();
        const object = SchemaObject.createNew(key, def);
        this.objects.push(object);
        this.evolver.addObject(object);

        return object;
    }

    findObjectByIri(iri: Iri): SchemaObject | undefined {
        return this.objects.find(object => object.iri === iri);
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        if ('iri' in def)
            this.notAvailableIris.add(def.iri);

        const signature = this._signatureProvider.createAndAdd();
        const morphism = SchemaMorphism.createNew(signature, def);
        this.morphisms.push(morphism);
        this.evolver.addMorphism(morphism);

        return morphism;
    }

    isIriAvailable(iri: Iri): boolean {
        return !this.notAvailableIris.has(iri);
    }

    editMorphism(morphism: SchemaMorphism, dom: SchemaObject, cod: SchemaObject, min: Min, label: string) {
        morphism.update(dom.key, cod.key, min, label);
    }

    deleteObject(object: SchemaObject) {
        this.evolver.deleteObject(object);
    }

    deleteMorphism(morphism: SchemaMorphism) {
        this.evolver.deleteMorphism(morphism);
    }

    getUpdateObject(): SchemaCategoryUpdate | null {
        const operations: SMOFromServer[] = [];
        for (const operation of this.evolver.getOperations()) {
            const operationToServer = operation.toServer();
            if (!operationToServer)
                return null;

            operations.push(operationToServer);
        }

        return {
            beforeVersion: this.version,
            operations,
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

        const rootObject = this.objects.find(object => object.key.equals(mapping.rootObjectKey));
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
        public readonly version: Version,
    ) {}

    static fromServer(input: SchemaCategoryInfoFromServer): SchemaCategoryInfo {
        return new SchemaCategoryInfo(
            input.id,
            input.label,
            input.version,
        );
    }
}

export type SchemaCategoryInit = {
    label: string;
};

export type SchemaCategoryUpdate = {
    readonly beforeVersion: Version;
    readonly operations: SMOFromServer[];
};
