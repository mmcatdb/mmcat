import { isPositionEqual } from '@/types/categoryGraph';
import { type SchemaCategory, type ObjexDefinition, SchemaObjex, type MorphismDefinition, SchemaMorphism, MetadataObjex, MetadataMorphism } from '@/types/schema';
import type { Result } from '../api/result';
import { CreateMorphism, CreateObjex, Composite, DeleteMorphism, DeleteObjex, type SMO, UpdateMorphism, UpdateObjex } from '../schema/operation';
import type { SchemaUpdate, SchemaUpdateInit } from '../schema/SchemaUpdate';
import type { MMO } from './metadata/mmo';
import { MorphismMetadata } from './metadata/morphismMetadata';
import { ObjexMetadata } from './metadata/objexMetadata';
import type { ObjexIds } from '../identifiers';
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

        this.schemaCategory = result.data;
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
        const createdObjexes = new Set(
            schemaOperations.filter((o): o is CreateObjex => o instanceof CreateObjex).map(o => o.objex.key.value),
        );
        const deletedObjexes = new Set(
            schemaOperations.filter((o): o is DeleteObjex => o instanceof DeleteObjex).map(o => o.objex.key.value),
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

    // FIXME remove
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
    createObjex(def: ObjexDefinition): SchemaObjex {
        const objex = this.schemaCategory.createObjex();
        const schemaObjex = SchemaObjex.createNew(objex.key, def);
        const operation = CreateObjex.create(schemaObjex);
        this.addOperation(operation);

        objex.metadata = MetadataObjex.create(def.label, { x: 0, y: 0 });

        return schemaObjex;
    }

    deleteObjex(schemaObjex: SchemaObjex) {
        const operation = DeleteObjex.create(schemaObjex);
        this.addOperation(operation);
    }

    updateObjex(oldSchemaObjex: SchemaObjex, update: {
        label?: string;
        ids?: ObjexIds | null;
    }): SchemaObjex {
        const newSchemaObjex = oldSchemaObjex.update(update);
        if (newSchemaObjex) {
            const operation = UpdateObjex.create(newSchemaObjex, oldSchemaObjex);
            this.addOperation(operation);
        }

        const objex = this.schemaCategory.getObjex(oldSchemaObjex.key);
        if (update.label && update.label !== objex.metadata.label)
            objex.metadata = MetadataObjex.create(update.label, objex.metadata.position);

        return newSchemaObjex ?? oldSchemaObjex;
    }

    createMorphism(def: MorphismDefinition): SchemaMorphism {
        const morphism = this.schemaCategory.createMorphism();
        const schemaMorphism = SchemaMorphism.createNew(morphism.signature, def);
        const operation = CreateMorphism.create(schemaMorphism);
        this.addOperation(operation);

        return schemaMorphism;
    }

    deleteMorphism(schemaMorphism: SchemaMorphism) {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (only if the cardinality changed).
        const operation = DeleteMorphism.create(schemaMorphism);
        this.addOperation(operation);
    }

    updateMorphism(oldSchemaMorphism: SchemaMorphism, update: Partial<MorphismDefinition>): SchemaMorphism {
        const newSchemaMorphism = oldSchemaMorphism.update(update);
        if (newSchemaMorphism) {
            const operation = UpdateMorphism.create(newSchemaMorphism, oldSchemaMorphism);
            this.addOperation(operation);
        }

        const morphism = this.schemaCategory.getMorphism(oldSchemaMorphism.signature);
        if (update.label && update.label !== morphism.metadata.label)
            morphism.metadata = MetadataMorphism.create(update.label);

        return newSchemaMorphism ?? oldSchemaMorphism;
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
