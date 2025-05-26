import { isPositionEqual, type Graph, type ListenerSession } from '@/types/categoryGraph';
import { type Category, type ObjexDefinition, SchemaObjex, type MorphismDefinition, SchemaMorphism, MetadataObjex, MetadataMorphism } from '@/types/schema';
import type { Result } from '../api/result';
import { CreateMorphism, CreateObjex, Composite, DeleteMorphism, DeleteObjex, type SMO, UpdateMorphism, UpdateObjex } from '../schema/operation';
import type { SchemaUpdate, SchemaUpdateInit } from '../schema/SchemaUpdate';
import type { MMO } from './metadata/mmo';
import { MorphismMetadata } from './metadata/morphismMetadata';
import { ObjexMetadata } from './metadata/objexMetadata';
import type { ObjexIds } from '../identifiers';
import type { LogicalModel } from '../datasource';

type UpdateFunction = (udpate: SchemaUpdateInit, logicalModels: LogicalModel[]) => Promise<Result<Category>>;

export type EvocatApi = {
    update: UpdateFunction;
};

export class Evocat {
    readonly uncommitedOperations = new SmoContext();

    private constructor(
        public schemaCategory: Category,
        private readonly updates: SchemaUpdate[],
        private readonly logicalModels: LogicalModel[],
        private readonly api: EvocatApi,
    ) {
    }

    static create(schemaCategory: Category, updates: SchemaUpdate[], logicalModels: LogicalModel[], api: EvocatApi): Evocat {
        const evocat = new Evocat(
            schemaCategory,
            updates,
            logicalModels,
            api,
        );

        return evocat;
    }

    async update() {
        const edit = this.getEdit();

        const result = await this.api.update(edit, this.logicalModels);
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
            node.objex.metadata = new MetadataObjex(node.objex.metadata.label, newPosition);
        });
    }

    private addOperation(smo: SMO) {
        this.uncommitedOperations.add(smo);
        smo.up(this.schemaCategory);
    }

    private getEdit(): SchemaUpdateInit {
        const schemaOperations = this.uncommitedOperations.collectAndReset();
        const schemaToServer = schemaOperations.map(operation => operation.toServer());

        return {
            prevVersion: this.schemaCategory.versionId,
            schema: schemaToServer,
            metadata: this.getMetadataUpdates(schemaOperations).map(operation => operation.toServer()),
        };
    }

    private getMetadataUpdates(schemaOperations: SMO[]): MMO[] {
        const createdObjexes = new Set(
            schemaOperations.filter((o): o is CreateObjex => o instanceof CreateObjex).map(o => o.schema.key.value),
        );
        const deletedObjexes = new Set(
            schemaOperations.filter((o): o is DeleteObjex => o instanceof DeleteObjex).map(o => o.schema.key.value),
        );

        const output: MMO[] = [];
        this.schemaCategory.getObjexes().forEach(objex => {
            const metadata = objex.metadata;
            const og = objex.originalMetadata;

            if (createdObjexes.has(objex.key.value)) {
                output.push(ObjexMetadata.create(objex.key, metadata));
                return;
            }

            if (deletedObjexes.has(objex.key.value)) {
                output.push(ObjexMetadata.create(objex.key, undefined, og));
                return;
            }

            if (isPositionEqual(og.position, metadata.position) && og.label === metadata.label)
                return;

            output.push(ObjexMetadata.create(objex.key, metadata, og));
        });

        const createdMorphisms = new Set(
            schemaOperations.filter((o): o is CreateMorphism => o instanceof CreateMorphism).map(o => o.schema.signature.value),
        );
        const deletedMorphisms = new Set(
            schemaOperations.filter((o): o is DeleteMorphism => o instanceof DeleteMorphism).map(o => o.schema.signature.value),
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

        const operation = new Composite(name, children);
        this.addOperation(operation);
    }

    /**
     * Creates a completely new schema with a key that has never been seen before.
     */
    createObjex(def: ObjexDefinition): SchemaObjex {
        const versionedObjex = this.schemaCategory.createObjex();
        versionedObjex.metadata = new MetadataObjex(def.label, { x: 0, y: 0 });
        const objex = SchemaObjex.createNew(versionedObjex.key, def);
        const operation = new CreateObjex(objex, versionedObjex.metadata);
        this.addOperation(operation);


        return objex;
    }

    deleteObjex(objex: SchemaObjex) {
        const metadata = this.schemaCategory.getObjex(objex.key).metadata;
        const operation = new DeleteObjex(objex, metadata);
        this.addOperation(operation);
    }

    updateObjex(oldObjex: SchemaObjex, update: {
        label?: string;
        ids?: ObjexIds | null;
    }): SchemaObjex {
        const newObjex = oldObjex.update(update);
        if (newObjex) {
            const operation = UpdateObjex.create(newObjex, oldObjex);
            this.addOperation(operation);
        }

        const versionedObjex = this.schemaCategory.getObjex(oldObjex.key);
        if (update.label && update.label !== versionedObjex.metadata.label)
            versionedObjex.metadata = new MetadataObjex(update.label, versionedObjex.metadata.position);

        return newObjex ?? oldObjex;
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        const versionedMorphism = this.schemaCategory.createMorphism();
        versionedMorphism.metadata = new MetadataMorphism(def.label ?? '');
        const morphism = SchemaMorphism.createNew(versionedMorphism.signature, def);
        const operation = new CreateMorphism(morphism, versionedMorphism.metadata);
        this.addOperation(operation);

        return morphism;
    }

    deleteMorphism(morphism: SchemaMorphism) {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (only if the cardinality changed).
        const metadata = this.schemaCategory.getMorphism(morphism.signature).metadata;
        const operation = new DeleteMorphism(morphism, metadata);
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
            versionedMorphism.metadata = new MetadataMorphism(update.label);

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
