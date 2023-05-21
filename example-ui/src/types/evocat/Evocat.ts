import type { Graph } from "@/types/categoryGraph";
import type { SchemaCategory, ObjectDefinition, SchemaObject, MorphismDefinition, SchemaMorphism } from "@/types/schema";
import type { IdDefinition } from "@/types/identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Result } from "../api/result";
import { Version, VersionContext } from "./Version";
import { CreateMorphism, CreateObject, Composite, DeleteMorphism, DeleteObject, type SMO } from "../schema/SchemaModificationOperation";
import type { SchemaUpdateInit } from "../schema/SchemaUpdate";
import { VersionedSMO } from "../schema/VersionedSMO";

type UpdateFunction = (udpate: SchemaUpdateInit) => Promise<Result<SchemaCategory>>;

export type EvocatApi = {
    update: UpdateFunction;
};

export class Evocat {
    readonly versionContext = VersionContext.createNew();

    private constructor(
        public schemaCategory: SchemaCategory,
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

    async update() {
        const updateObject = this.getUpdateObject();

        const result = await this.api.update(updateObject);
        if (!result.status)
            return;

        const beforeCategory = this.schemaCategory;
        this.schemaCategory = result.data;
        this.schemaCategory.graph = beforeCategory.graph;
    }

    get graph(): Graph | undefined {
        return this.schemaCategory.graph;
    }

    set graph(newGraph: Graph | undefined) {
        this.schemaCategory.graph = newGraph;
    }

    private readonly operations: Map<string, VersionedSMO> = new Map();

    private commitOperation(smo: SMO) {
        const version = this.versionContext.createNextVersion();

        this.operations.set(version.id, VersionedSMO.create(version, smo));
        console.log(`[${version}] : ${smo.type}`);

        smo.up(this.schemaCategory);
    }

    private getUpdateObject(): SchemaUpdateInit {
        const operations = [ ...this.operations.values() ]
            .filter(operation => operation.isNew)
            .sort((a, b) => a.version.compare(b.version))
            .map(operation => operation.toServer());

        return {
            beforeVersion: this.schemaCategory.versionId,
            operations,
        };
    }

    undo(skipLowerLevels = true) {
        this.versionContext.undo(skipLowerLevels).forEach(version => {
            const operation = this.operations.get(version.id)?.smo;
            if (!operation)
                throw new Error(`Undo error: Operation for version: ${version} not found.`);

            operation.down(this.schemaCategory);
        });
    }

    redo(skipLowerLevels = true) {
        this.versionContext.redo(skipLowerLevels).forEach(version => {
            const operation = this.operations.get(version.id)?.smo;
            if (!operation)
                throw new Error(`Redo error: Operation for version: ${version} not found.`);

            operation.up(this.schemaCategory);
        });
    }

    move(target: Version) {
        const { undo, redo } = this.versionContext.move(target);

        undo.forEach(version => {
            const operation = this.operations.get(version.id)?.smo;
            if (!operation)
                throw new Error(`Move error: Operation for version: ${version} not found.`);

            operation.down(this.schemaCategory);
        });

        redo.forEach(version => {
            const operation = this.operations.get(version.id)?.smo;
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

    createObject(def: ObjectDefinition): SchemaObject {
        const object = this.schemaCategory.createObject(def);
        const operation = CreateObject.create(object);
        this.commitOperation(operation);

        return object;
    }

    deleteObject(object: SchemaObject): void {
        const operation = new DeleteObject(object);
        this.commitOperation(operation);
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        const morphism = this.schemaCategory.createMorphism(def);
        const operation = new CreateMorphism(morphism);
        this.commitOperation(operation);

        return morphism;
    }

    deleteMorphism(morphism: SchemaMorphism): void {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).
        const operation = new DeleteMorphism(morphism);
        this.commitOperation(operation);
    }

    createId(object: SchemaObject, def: IdDefinition): void {
        object.addId(def);

        //const node = this._graph?.getNode(object);
        //node?.updateNoIdsClass();

        //this.createOperation('addId');
    }
}
