import type { Version } from "../id";
import type { SchemaMorphism } from "./SchemaMorphism";
import type { SchemaObject } from "./SchemaObject";

export type SchemaCategoryUpdate = {
    readonly beforeVersion: Version;
    readonly operations: SchemaModificationOperation[];
};

enum SMOType {
    AddObject = 'addObject'
}

export type SchemaModificationOperation = {
    type: SMOType;
};

export class SchemaCategoryEvolver {
    _newObjects = [] as SchemaObject[];
    _newMorphisms = [] as SchemaMorphism[];

    addObject(object: SchemaObject) {
        this._newObjects.push(object);
    }

    deleteObject(object: SchemaObject) {
        this._newObjects = this._newObjects.filter(o => !o.equals(object));
    }

    addMorphism(morphism: SchemaMorphism) {
        this._newMorphisms.push(morphism);
    }

    deleteMorphismWithDual(morphism: SchemaMorphism) {
        this._newMorphisms = this._newMorphisms.filter(m => !m.equals(morphism) && !m.equals(morphism.dual));
    }

    generateOperations(): SchemaModificationOperation[] {

        /*
        return {
            objects: this._newObjects.map(object => ({
                temporaryId: object.id,
                position: object.position,
                jsonValue: JSON.stringify(object.toJSON())
            })),
            morphisms: this._newMorphisms.map(morphism => ({
                domId: morphism.domId <= this._maxExistingObjectId ? morphism.domId : undefined,
                codId: morphism.codId <= this._maxExistingObjectId ? morphism.codId : undefined,
                temporaryDomId: morphism.domId > this._maxExistingObjectId ? morphism.domId : undefined,
                temporaryCodId: morphism.codId > this._maxExistingObjectId ? morphism.codId : undefined,
                jsonValue: JSON.stringify(morphism.toJSON())
            }))
        };
        */

        return [];
    }
}
