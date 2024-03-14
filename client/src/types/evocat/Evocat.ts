import type { Graph } from '@/types/categoryGraph';
import { type SchemaCategory, type ObjectDefinition, SchemaObject, type MorphismDefinition, SchemaMorphism } from '@/types/schema';
import type { LogicalModel } from '../logicalModel';
import type { Result } from '../api/result';
import { Version, VersionContext, computeLatestVersions } from './Version';
import { CreateMorphism, CreateObject, Composite, DeleteMorphism, DeleteObject, type SMO, EditMorphism, EditObject } from '../schema/operation';
import type { MetadataUpdate, SchemaUpdate, SchemaUpdateInit } from '../schema/SchemaUpdate';
import { VersionedSMO } from '../schema/VersionedSMO';

type UpdateFunction = (udpate: SchemaUpdateInit, logicalModels: LogicalModel[]) => Promise<Result<SchemaCategory>>;
type UpdateMetadataFunction = (metadata: MetadataUpdate[]) => Promise<void>;

export type EvocatApi = {
    update: UpdateFunction;
    updateMetadata: UpdateMetadataFunction;
};

export class Evocat {
    readonly versionContext;

    private constructor(
        public schemaCategory: SchemaCategory,
        private readonly updates: SchemaUpdate[],
        private readonly logicalModels: LogicalModel[],
        private readonly api: EvocatApi,
    ) {
        const operations = updates.flatMap(update => update.operations);
        this.operations = new Map(operations.map(operation => [ operation.version.id, operation ]));
        this.versionContext = VersionContext.create(operations.map(operation => operation.version));
    }

    static create(schemaCategory: SchemaCategory, updates: SchemaUpdate[], logicalModels: LogicalModel[], api: EvocatApi): Evocat {
        const evocat = new Evocat(
            schemaCategory,
            updates,
            logicalModels,
            api,
        );

        return evocat;
    }

    async update() {
        const updateObject = this.getUpdateObject();
        if (updateObject.operations.length === 0) {
            // If there are no SMOs to create, we just update the metadata.
            await this.api.updateMetadata(updateObject.metadata);
            return;
        }

        const result = await this.api.update(updateObject, this.logicalModels);
        if (!result.status)
            return;

        const beforeCategory = this.schemaCategory;
        this.schemaCategory = result.data;
        this.schemaCategory.graph = beforeCategory.graph;
    }

    async updateMetadata() {
        await this.api.updateMetadata(this.getMetadataUpdates());
    }

    get graph(): Graph | undefined {
        return this.schemaCategory.graph;
    }

    set graph(newGraph: Graph | undefined) {
        this.schemaCategory.graph = newGraph;
    }

    private readonly operations: Map<string, VersionedSMO>;

    private commitOperation(smo: SMO) {
        const version = this.versionContext.createNextVersion();

        this.operations.set(version.id, VersionedSMO.create(version, smo));
        console.log(`[${version}] : ${smo.type}`);

        smo.up(this.schemaCategory);
    }

    private getUpdateObject(): SchemaUpdateInit {
        const newOperations = computeLatestVersions(this.versionContext.root)
            .map(version => this.operations.get(version.id))
            .filter((operation): operation is VersionedSMO => !!operation)
            .filter(operation => operation.isNew);

        const operationsToServer = newOperations.map(operation => operation.toServer());

        newOperations.forEach(operation => operation.isNew = false);
        const versionsToRemove =  [ ...this.operations.values() ]
            .filter(operation => operation.isNew)
            .map(operation => operation.version);

        versionsToRemove.forEach(version => this.operations.delete(version.id));
        this.versionContext.removeVersions(versionsToRemove);

        return {
            prevVersion: this.schemaCategory.versionId,
            operations: operationsToServer,
            metadata: this.getMetadataUpdates(),
        };
    }

    private getMetadataUpdates(): MetadataUpdate[] {
        return this.schemaCategory.getObjects().map(object => ({
            key: object.key.toServer(),
            position: object.position,
        }));
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

        const operation = Composite.create(name);
        this.commitOperation(operation);
    }

    /**
     * Creates a completely new schema object with key that has never been seen before.
     */
    createObject(def: ObjectDefinition): SchemaObject {
        const versionedObject = this.schemaCategory.createObject();
        const object = SchemaObject.createNew(versionedObject.key, def);
        const operation = CreateObject.create(object);
        this.commitOperation(operation);

        return object;
    }

    deleteObject(object: SchemaObject) {
        const operation = DeleteObject.create(object);
        this.commitOperation(operation);
    }

    editObject(update: ObjectDefinition, oldObject: SchemaObject): SchemaObject {
        const newObject = oldObject.createCopy(update);
        const operation = EditObject.create(newObject, oldObject);
        this.commitOperation(operation);

        return newObject;
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        const versionedMorphism = this.schemaCategory.createMorphism();
        const morphism = SchemaMorphism.createNew(versionedMorphism.signature, def);
        const operation = CreateMorphism.create(morphism);
        this.commitOperation(operation);

        return morphism;
    }

    deleteMorphism(morphism: SchemaMorphism) {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (only if the cardinality changed).
        const operation = DeleteMorphism.create(morphism);
        this.commitOperation(operation);
    }

    editMorphism(update: MorphismDefinition, oldMorphism: SchemaMorphism): SchemaMorphism {
        const newMorphism = oldMorphism.createCopy(update);
        const operation = EditMorphism.create(newMorphism, oldMorphism);
        this.commitOperation(operation);

        return newMorphism;
    }
}
