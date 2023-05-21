import type { Iri } from "@/types/integration";
import { UniqueIdProvider } from "@/utils/UniqueIdProvider";
import { ComplexProperty, type ParentProperty } from "@/types/accessPath/basic";
import type { Entity, Id, VersionId } from "../id";
import { DynamicName, Key, Signature, type IdDefinition } from "../identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Mapping } from "../mapping";
import { SchemaMorphism, type SchemaMorphismFromServer, type Min, type MorphismDefinition } from "./SchemaMorphism";
import { SchemaObject, type ObjectDefinition, type SchemaObjectFromServer } from "./SchemaObject";
import type { SMOFromServer } from "./SchemaModificationOperation";
import type { Graph } from "../categoryGraph";

export class SchemaCategory implements Entity {
    readonly id: Id;
    readonly label: string;
    readonly versionId: VersionId;
    objects: SchemaObject[];
    morphisms: SchemaMorphism[];
    notAvailableIris: Set<Iri> = new Set;

    _keysProvider = new UniqueIdProvider<Key>({ function: key => key.value, inversion: value => Key.createNew(value) });
    _signatureProvider = new UniqueIdProvider<Signature>({ function: signature => signature.baseValue ?? 0, inversion: value => Signature.base(value) });

    private constructor(id: Id, label: string, versionId: VersionId, objects: SchemaObject[], morphisms: SchemaMorphism[]) {
        this.id = id;
        this.label = label;
        this.versionId = versionId;
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

    getObject(key: Key): SchemaObject | undefined {
        return this.objects.find(object => object.key.equals(key));
    }

    createObject(def: ObjectDefinition): SchemaObject {
        if ('iri' in def)
            this.notAvailableIris.add(def.iri);

        const key = this._keysProvider.createAndAdd();
        return SchemaObject.createNew(key, def);
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        if ('iri' in def)
            this.notAvailableIris.add(def.iri);

        const signature = this._signatureProvider.createAndAdd();
        return SchemaMorphism.createNew(signature, def);
    }

    addObject(object: SchemaObject) {
        this.objects.push(object);
        this._graph?.createNode(object, 'new');
    }

    removeObject(object: SchemaObject) {
        // TODO make it map?
        this.objects = this.objects.filter(o => !o.equals(object));
        this._graph?.deleteNode(object);
    }

    findObjectByIri(iri: Iri): SchemaObject | undefined {
        return this.objects.find(object => object.iri === iri);
    }

    addMorphism(morphism: SchemaMorphism) {
        this.morphisms.push(morphism);
        this._graph?.createEdge(morphism, 'new');
    }

    removeMorphism(morphism: SchemaMorphism) {
        // TODO make it map?
        this.morphisms = this.morphisms.filter(m => !m.equals(morphism));
        this._graph?.deleteEdge(morphism);
    }

    addId(object: SchemaObject, def: IdDefinition): void {
        object.addId(def);

        const node = this._graph?.getNode(object);
        node?.updateNoIdsClass();
    }

    isIriAvailable(iri: Iri): boolean {
        return !this.notAvailableIris.has(iri);
    }

    editMorphism(morphism: SchemaMorphism, dom: SchemaObject, cod: SchemaObject, min: Min, label: string) {
        morphism.update(dom.key, cod.key, min, label);
    }

    getUpdateObject(): SchemaCategoryUpdate | null {
        const operations: SMOFromServer[] = [];
        // TODO
        /*
        for (const operation of this.evolver.getOperations()) {
            const operationToServer = operation.toServer();
            if (!operationToServer)
                return null;

            operations.push(operationToServer);
        }
        */

        return {
            beforeVersion: this.versionId,
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

    private _graph?: Graph;

    get graph(): Graph | undefined {
        return this._graph;
    }

    set graph(newGraph: Graph | undefined) {
        this._graph = newGraph;

        if (!newGraph)
            return;

        // TODO

        // more TODO

        /*
        this.logicalModels.forEach(logicalModel => {
            logicalModel.mappings.forEach(mapping => {
                this.schemaCategory.setDatabaseToObjectsFromMapping(mapping, logicalModel);
            });
        });
        */

        newGraph.getCytoscape().batch(() => {
            this.objects.forEach(object => newGraph.createNode(object));

            // First we create a dublets of morphisms. Then we create edges from them.
            // TODO there should only be base morphisms
            const sortedBaseMorphisms = this.morphisms.filter(morphism => morphism.isBase)
                .sort((m1, m2) => m1.sortBaseValue - m2.sortBaseValue);

            sortedBaseMorphisms.forEach(morphism => newGraph.createEdge(morphism));
        });

        // Position the object to the center of the canvas.
        newGraph.fixLayout();
        newGraph.layout();
        newGraph.center();
    }
}

export type SchemaCategoryFromServer = {
    id: Id;
    label: string;
    version: VersionId;
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
    version: VersionId;
};

export class SchemaCategoryInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly versionId: VersionId,
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
    readonly beforeVersion: VersionId;
    readonly operations: SMOFromServer[];
};
