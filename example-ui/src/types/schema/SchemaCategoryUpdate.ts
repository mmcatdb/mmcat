import type { Version } from "../id";
import { AddMorphism, AddObject, type SMO, type SMOFromServer } from "./SchemaModificationOperation";
import type { SchemaMorphism } from "./SchemaMorphism";
import type { SchemaObject } from "./SchemaObject";

export class SchemaCategoryEvolver {
    _newObjects = [] as SchemaObject[];
    _newMorphisms = [] as SchemaMorphism[];

    _operations = [] as SMO[];

    addObject(object: SchemaObject) {
        this._newObjects.push(object);
        this._operations.push(new AddObject(object));
    }

    deleteObject(object: SchemaObject) {
        this._newObjects = this._newObjects.filter(o => !o.equals(object));
    }

    addMorphism(morphism: SchemaMorphism) {
        this._newMorphisms.push(morphism);
        this._operations.push(new AddMorphism(morphism));
    }

    deleteMorphism(morphism: SchemaMorphism) {
        this._newMorphisms = this._newMorphisms.filter(m => !m.equals(morphism));
    }

    getOperations(): SMO[] {
        return this._operations;
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
    }
}
