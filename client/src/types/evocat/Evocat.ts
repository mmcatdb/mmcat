import { isPositionEqual, type Graph, type ListenerSession } from '@/types/categoryGraph';
import { type SchemaCategory, type ObjectDefinition, SchemaObject, type MorphismDefinition, SchemaMorphism, MetadataObject, MetadataMorphism } from '@/types/schema';
import type { Result } from '../api/result';
import { CreateMorphism, CreateObject, Composite, DeleteMorphism, DeleteObject, type SMO, UpdateMorphism, UpdateObject } from '../schema/operation';
import type { SchemaUpdate, SchemaUpdateInit } from '../schema/SchemaUpdate';
import type { MMO } from './metadata/mmo';
import { MorphismMetadata } from './metadata/morphismMetadata';
import { ObjectMetadata } from './metadata/objectMetadata';
import type { ObjectIds } from '../identifiers';
import { type LogicalModel } from '../datasource';

type UpdateFunction = (udpate: SchemaUpdateInit, logicalModels: LogicalModel[]) => Promise<Result<SchemaCategory>>;

export type EvocatApi = {
    update: UpdateFunction;
};

export class Evocat {
    readonly uncommitedOperations = new SmoContext();

    private constructor(
        public schemaCategory: SchemaCategory,
        private readonly updates: SchemaUpdate[],
        private readonly logicalModels: LogicalModel[],
        private readonly api: EvocatApi,
    ) {
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

        const result = await this.api.update(updateObject, this.logicalModels);
        if (!result.status)
            return;

        const beforeCategory = this.schemaCategory;
        this.schemaCategory = result.data;
        this.schemaCategory.graph = beforeCategory.graph;
    }

    private graphListener?: ListenerSession;

    get graph(): Graph | undefined {
        return this.schemaCategory.graph;
    }

    set graph(newGraph: Graph | undefined) {
        this.graphListener?.close();
        this.schemaCategory.graph = newGraph;
        this.graphListener = this.schemaCategory.graph?.listen();
        this.graphListener?.onNode('dragfreeon', node => {
            const newPosition = { ...node.cytoscapeIdAndPosition.position };
            node.object.metadata = MetadataObject.create(node.object.metadata.label, newPosition);
        });
    }

    private addOperation(smo: SMO) {
        this.uncommitedOperations.add(smo);
        smo.up(this.schemaCategory);
    }

    private getUpdateObject(): SchemaUpdateInit {
        const schemaOperations = this.uncommitedOperations.collectAndReset();
        const schemaToServer = schemaOperations.map(operation => operation.toServer());

        return {
            prevVersion: this.schemaCategory.versionId,
            schema: schemaToServer,
            metadata: this.getMetadataUpdates(schemaOperations).map(operation => operation.toServer()),
        };
    }

    private getMetadataUpdates(schemaOperations: SMO[]): MMO[] {
        const createdObjects = new Set(
            schemaOperations.filter((o): o is CreateObject => o instanceof CreateObject).map(o => o.object.key.value),
        );
        const deletedObjects = new Set(
            schemaOperations.filter((o): o is DeleteObject => o instanceof DeleteObject).map(o => o.object.key.value),
        );

        const output: MMO[] = [];
        this.schemaCategory.getObjects().forEach(object => {
            const metadata = object.metadata;
            const og = object.originalMetadata;

            if (createdObjects.has(object.key.value)) {
                output.push(ObjectMetadata.create(object.key, metadata));
                return;
            }

            if (deletedObjects.has(object.key.value)) {
                output.push(ObjectMetadata.create(object.key, undefined, og));
                return;
            }

            if (isPositionEqual(og.position, metadata.position) && og.label === metadata.label)
                return;

            output.push(ObjectMetadata.create(object.key, metadata, og));
        });

        const createdMorphisms = new Set(
            schemaOperations.filter((o): o is CreateMorphism => o instanceof CreateMorphism).map(o => o.morphism.signature.value),
        );
        const deletedMorphisms = new Set(
            schemaOperations.filter((o): o is DeleteMorphism => o instanceof DeleteMorphism).map(o => o.morphism.signature.value),
        );

        this.schemaCategory.getMorphisms().forEach(morphism => {
            const metadata = morphism.metadata;
            const og = morphism.originalMetadata;

            if (createdMorphisms.has(morphism.signature.value)) {
                output.push(MorphismMetadata.create(morphism.signature, metadata));
                return;
            }

            if (deletedMorphisms.has(morphism.signature.value)) {
                output.push(MorphismMetadata.create(morphism.signature, undefined, og));
                return;
            }

            if (og.label === metadata.label)
                return;

            output.push(MorphismMetadata.create(morphism.signature, metadata, og));
        });

        return output;
    }

    // undo(skipLowerLevels = true) {
    //     this.versionContext.undo(skipLowerLevels).forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Undo error: Operation for version: ${version} not found.`);

    //         operation.down(this.schemaCategory);
    //     });
    // }

    // redo(skipLowerLevels = true) {
    //     this.versionContext.redo(skipLowerLevels).forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Redo error: Operation for version: ${version} not found.`);

    //         operation.up(this.schemaCategory);
    //     });
    // }

    // move(target: Version) {
    //     const { undo, redo } = this.versionContext.move(target);

    //     undo.forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Move error: Operation for version: ${version} not found.`);

    //         operation.down(this.schemaCategory);
    //     });

    //     redo.forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Move error: Operation for version: ${version} not found.`);

    //         operation.up(this.schemaCategory);
    //     });
    // }

    compositeOperation<T = void>(name: string, callback: () => T): T {
        this.uncommitedOperations.down();
        const result = callback();

        this.finishCompositeOperation(name);

        return result;
    }

    finishCompositeOperation(name: string) {
        const children = this.uncommitedOperations.tryUp();
        if (!children)
            throw new Error('Composite operation finished before it was started.');

        const operation = Composite.create(name, children);
        this.addOperation(operation);
    }

    /**
     * Creates a completely new schema object with a key that has never been seen before.
     */
    createObject(def: ObjectDefinition): SchemaObject {
        const versionedObject = this.schemaCategory.createObject();
        const object = SchemaObject.createNew(versionedObject.key, def);
        const operation = CreateObject.create(object);
        this.addOperation(operation);

        versionedObject.metadata = MetadataObject.create(def.label, { x: 0, y: 0 });

        return object;
    }

    deleteObject(object: SchemaObject) {
        const operation = DeleteObject.create(object);
        this.addOperation(operation);
    }

    updateObject(oldObject: SchemaObject, update: {
        label?: string;
        ids?: ObjectIds | null;
    }): SchemaObject {
        const newObject = oldObject.update(update);
        if (newObject) {
            const operation = UpdateObject.create(newObject, oldObject);
            this.addOperation(operation);
        }

        const versionedObject = this.schemaCategory.getObject(oldObject.key);
        if (update.label && update.label !== versionedObject.metadata.label)
            versionedObject.metadata = MetadataObject.create(update.label, versionedObject.metadata.position);

        return newObject ?? oldObject;
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        const versionedMorphism = this.schemaCategory.createMorphism();
        const morphism = SchemaMorphism.createNew(versionedMorphism.signature, def);
        const operation = CreateMorphism.create(morphism);
        this.addOperation(operation);

        return morphism;
    }

    deleteMorphism(morphism: SchemaMorphism) {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (only if the cardinality changed).
        const operation = DeleteMorphism.create(morphism);
        this.addOperation(operation);
    }

    updateMorphism(oldMorphism: SchemaMorphism, update: Partial<MorphismDefinition>): SchemaMorphism {
        const newMorphism = oldMorphism.update(update);
        if (newMorphism) {
            const operation = UpdateMorphism.create(newMorphism, oldMorphism);
            this.addOperation(operation);
        }

        const versionedMorphism = this.schemaCategory.getMorphism(oldMorphism.signature);
        if (update.label && update.label !== versionedMorphism.metadata.label)
            versionedMorphism.metadata = MetadataMorphism.create(update.label);

        return newMorphism ?? oldMorphism;
    }
}

class SmoContext {
    private levels: SMO[][] = [ [] ];

    down() {
        this.levels.push([]);
    }

    tryUp(): SMO[] | undefined {
        return this.levels.pop();
    }

    add(smo: SMO) {
        this.levels[this.levels.length - 1].push(smo);
    }

    isRoot() {
        return this.levels.length === 1;
    }

    collectAndReset(): SMO[] {
        if (!this.isRoot())
            throw new Error('Cannot reset SMO context when not at root level.');

        const smo = this.levels[0];
        this.levels = [ [] ];

        return smo;
    }
}

type UserAction = SMO | MMO;

/**
 * Action is anything that can be undone or redone.
 * E.g., SMOs, position changes, etc.
 */
class ActionContext {
    private actions: UserAction[] = [];
    private current = 0;

    undo() {
    }

    redo() {
    }
}
