import { type Category, type ObjexDefinition, SchemaObjex, type MorphismDefinition, SchemaMorphism, MetadataObjex, MetadataMorphism, isPositionEqual, type Position } from '@/types/schema';
import type { Result } from '../api/result';
import { CreateMorphism, CreateObjex, Composite, DeleteMorphism, DeleteObjex, type SMO, UpdateMorphism, UpdateObjex } from './schema';
import type { SchemaUpdate, SchemaUpdateInit } from '../schema/SchemaUpdate';
import type { MMO } from './metadata/mmo';
import { MorphismMetadata } from './metadata/morphismMetadata';
import { ObjexMetadata } from './metadata/objexMetadata';
import type { Key, ObjexIds, Signature } from '../identifiers';

type UpdateApi = (udpate: SchemaUpdateInit) => Promise<Result<Category>>;

/**
 * This class tracks multiple versions of given {@link Category}.
 * It can change the current version by applying/undoing SMOs. It also tracks all new SMOs and can commit them to the server.
 */
export class Evocat {
    readonly uncommitedOperations = new SmoContext();

    constructor(
        private _category: Category,
        private readonly _updates: SchemaUpdate[],
    ) {}

    get category() {
        return this._category;
    }

    get updates() {
        return this._updates;
    }

    async update(api: UpdateApi) {
        const edit = this.getEdit();

        const result = await api(edit);
        if (!result.status)
            return;

        this._category = result.data;
    }

    private addOperation(smo: SMO) {
        this.uncommitedOperations.add(smo);
        smo.up(this._category);
    }

    private getEdit(): SchemaUpdateInit {
        const schemaOperations = this.uncommitedOperations.collectAndReset();
        const schemaToServer = schemaOperations.map(operation => operation.toServer());

        return {
            prevVersion: this._category.versionId,
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
        this._category.getObjexes().forEach(objex => {
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

        this._category.getMorphisms().forEach(morphism => {
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

    //         operation.down(this._category);
    //     });
    // }

    // redo(skipLowerLevels = true) {
    //     this.versionContext.redo(skipLowerLevels).forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Redo error: Operation for version: ${version} not found.`);

    //         operation.up(this._category);
    //     });
    // }

    // move(target: Version) {
    //     const { undo, redo } = this.versionContext.move(target);

    //     undo.forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Move error: Operation for version: ${version} not found.`);

    //         operation.down(this._category);
    //     });

    //     redo.forEach(version => {
    //         const operation = this.operations.get(version.id)?.smo;
    //         if (!operation)
    //             throw new Error(`Move error: Operation for version: ${version} not found.`);

    //         operation.up(this._category);
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
     * Creates a completely new objex with a key that has never been seen before.
     * @returns The key.
     */
    createObjex(def: ObjexDefinition): Key {
        const key = this._category.createKey();
        const schema = SchemaObjex.createNew(key, def);
        const metadata = new MetadataObjex(def.label, def.position);
        const operation = new CreateObjex(schema, metadata);

        this.addOperation(operation);
        return key;
    }

    deleteObjex(key: Key) {
        const objex = this._category.getObjex(key);
        objex.findNeighborMorphisms().forEach(morphism => this.deleteMorphism(morphism.signature));

        const { schema, metadata } = objex;
        const operation = new DeleteObjex(schema, metadata);
        this.addOperation(operation);
    }

    updateObjex(key: Key, update: {
        label?: string;
        ids?: ObjexIds;
        position?: Position;
    }) {
        const objex = this._category.getObjex(key);
        const { schema, metadata } = objex;

        const newSchema = schema.update(update);
        if (newSchema) {
            const operation = UpdateObjex.create(newSchema, schema);
            this.addOperation(operation);
        }

        const newLabel = update.label ?? metadata.label;
        const newPosition = update.position ?? metadata.position;
        const doUpdateMetadata = (newLabel !== metadata.label) || !isPositionEqual(newPosition, metadata.position);
        if (doUpdateMetadata)
            objex.metadata = new MetadataObjex(newLabel, newPosition);

        // TODO Something here? Or is this enough?
    }

    /**
     * Creates a completely new morphism with a signature that has never been seen before.
     * @returns The signature.
     */
    createMorphism(def: MorphismDefinition): Signature {
        const signature = this._category.createSignature();
        const schema = SchemaMorphism.createNew(signature, def);
        const metadata = new MetadataMorphism(def.label);
        const operation = new CreateMorphism(schema, metadata);

        this.addOperation(operation);
        return signature;
    }

    deleteMorphism(signature: Signature) {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (only if the cardinality changed).

        const { schema, metadata } = this._category.getMorphism(signature);
        const operation = new DeleteMorphism(schema, metadata);
        this.addOperation(operation);
    }

    updateMorphism(oldSchemaMorphism: SchemaMorphism, update: Partial<MorphismDefinition>) {
        const newSchemaMorphism = oldSchemaMorphism.update(update);
        if (newSchemaMorphism) {
            const operation = UpdateMorphism.create(newSchemaMorphism, oldSchemaMorphism);
            this.addOperation(operation);
        }

        // FIXME
        const morphism = this._category.getMorphism(oldSchemaMorphism.signature);
        if (update.label && update.label !== morphism.metadata.label)
            morphism.metadata = new MetadataMorphism(update.label);
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

    hasUnsavedChanges() {
        return this.levels.some(level => level.length > 0);
    }
}
