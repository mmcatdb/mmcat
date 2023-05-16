import type { Graph } from "@/types/categoryGraph";
import type { SchemaCategory, ObjectDefinition, SchemaObject, MorphismDefinition, SchemaMorphism, SchemaCategoryUpdate } from "@/types/schema";
import type { IdDefinition } from "@/types/identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Result } from "../api/result";
import { Version, VersionContext } from "./Version";
import { AddMorphism, AddObject, Composite, DeleteMorphism, DeleteObject, type SMO } from "../schema/SchemaModificationOperation";

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

    undo(skipLowerLevels = true) {
        this.versionContext.undo(skipLowerLevels).forEach(version => {
            const operation = this.operations.get(version.id)?.operation;
            if (!operation)
                throw new Error(`Undo error: Operation for version: ${version} not found.`);

            operation.down(this.schemaCategory);
        });
    }

    redo(skipLowerLevels = true) {
        this.versionContext.redo(skipLowerLevels).forEach(version => {
            const operation = this.operations.get(version.id)?.operation;
            if (!operation)
                throw new Error(`Redo error: Operation for version: ${version} not found.`);

            operation.up(this.schemaCategory);
        });
    }

    move(target: Version) {
        const { undo, redo } = this.versionContext.move(target);

        undo.forEach(version => {
            const operation = this.operations.get(version.id)?.operation;
            if (!operation)
                throw new Error(`Move error: Operation for version: ${version} not found.`);

            operation.down(this.schemaCategory);
        });

        redo.forEach(version => {
            const operation = this.operations.get(version.id)?.operation;
            if (!operation)
                throw new Error(`Move error: Operation for version: ${version} not found.`);

            operation.up(this.schemaCategory);
        });
    }

    compositeOperation<T = void>(name: string, callback: () => T): T {
        this.versionContext.nextLevel();
        const result = callback();

        this.finishCompositeOperation(name);

        return result;
    }

    finishCompositeOperation(name: string) {
        this.versionContext.prevLevel();

        const operation = new Composite(name);
        this.commitOperation(operation);
    }

    addObject(def: ObjectDefinition): SchemaObject {
        const object = this.schemaCategory.createObject(def);
        const operation = new AddObject(object);
        this.commitOperation(operation);

        return object;
    }

    removeObject(object: SchemaObject): void {
        const operation = new DeleteObject(object);
        this.commitOperation(operation);
    }

    addMorphism(def: MorphismDefinition): SchemaMorphism {
        const morphism = this.schemaCategory.createMorphism(def);
        const operation = new AddMorphism(morphism);
        this.commitOperation(operation);

        return morphism;
    }

    removeMorphism(morphism: SchemaMorphism): void {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).
        const operation = new DeleteMorphism(morphism);
        this.commitOperation(operation);
    }

    addId(object: SchemaObject, def: IdDefinition): void {
        object.addId(def);

        //const node = this._graph?.getNode(object);
        //node?.updateNoIdsClass();

        //this.createOperation('addId');
    }
}
