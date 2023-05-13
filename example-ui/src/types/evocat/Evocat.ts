import type { Graph } from "@/types/categoryGraph";
import type { SchemaCategory, ObjectDefinition, SchemaObject, MorphismDefinition, SchemaMorphism, SchemaCategoryUpdate } from "@/types/schema";
import type { IdDefinition } from "@/types/identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Result } from "../api/result";
import { Version, VersionContext } from "./Version";
import { AddObject, type SMO } from "../schema/SchemaModificationOperation";

type UpdateFunction = (udpate: SchemaCategoryUpdate) => Promise<Result<SchemaCategory>>;

export type EvocatApi = {
    update: UpdateFunction;
};

export class Evocat {
    readonly versionContext = VersionContext.createNew();

    private constructor(
        readonly schemaCategory: SchemaCategory,
        readonly logicalModels: LogicalModel[],
        readonly api: EvocatApi,
    ) {}

    static create(schemaCategory: SchemaCategory, logicalModels: LogicalModel[], api: EvocatApi): Evocat {
        const evocat = new Evocat(
            schemaCategory,
            logicalModels,
            api,
        );

        return evocat;
    }

    get graph(): Graph | undefined {
        return this.schemaCategory.graph;
    }

    set graph(newGraph: Graph | undefined) {
        this.schemaCategory.graph = newGraph;
    }

    private readonly operations = new Map as Map<string, { operation: SMO, version: Version }>;

    private commitOperation(operation: SMO) {
        const version = this.versionContext.createNextVersion();
        this.operations.set(version.id, { operation, version });
        console.log(`[${version}] : ${operation.type}`);

        operation.up(this.schemaCategory);
    }

    undo() {
        const prev = this.versionContext.currentVersion.parent;
        if (!prev)
            return;

        const operation = this.operations.get(this.versionContext.currentVersion.id)?.operation;
        if (!operation)
            return;

        operation.down(this.schemaCategory);
        this.versionContext.currentVersion = prev;
    }

    redo() {
        const next = this.versionContext.currentVersion.firstChild;
        if (!next)
            return;

        const operation = this.operations.get(next.id)?.operation;
        if (!operation)
            return;

        operation.up(this.schemaCategory);
        this.versionContext.currentVersion = next;
    }

    compositeOperation<T = void>(name: string, callback: () => T): T {
        this.versionContext.nextLevel();
        const result = callback();
        this.versionContext.prevLevel();

        // TODO create composite operation
        //this.createOperation(name);

        return result;
    }

    addObject(def: ObjectDefinition): SchemaObject {
        const object = this.schemaCategory.createObject(def);
        //this._graph?.createNode(object, 'new');

        const operation = new AddObject(object);
        this.commitOperation(operation);

        //this.createOperation('addObject');

        return object;
    }

    removeObject(object: SchemaObject): void {
        this.schemaCategory.deleteObject(object);
        //this._graph?.deleteNode(object);

        //this.createOperation('removeObject');
    }

    addMorphism(def: MorphismDefinition): SchemaMorphism {
        const morphism = this.schemaCategory.createMorphism(def);
        //this._graph?.createEdge(morphism, 'new');

        //this.createOperation('addMorphism');

        return morphism;
    }

    removeMorphism(morphism: SchemaMorphism): void {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).
        this.schemaCategory.deleteMorphism(morphism);
        //this._graph?.deleteEdge(morphism);

        //this.createOperation('removeMorphism');
    }

    addId(object: SchemaObject, def: IdDefinition): void {
        object.addId(def);

        //const node = this._graph?.getNode(object);
        //node?.updateNoIdsClass();

        //this.createOperation('addId');
    }
}
