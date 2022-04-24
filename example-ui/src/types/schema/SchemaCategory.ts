import { Key } from "../identifiers";
import { SchemaMorphism, SchemaMorphismFromServer } from "./SchemaMorphism";
import { SchemaObject, SchemaObjectFromServer } from "./SchemaObject";

export class SchemaCategory {
    id!: number;
    jsonValue!: string;
    objects!: SchemaObject[];
    morphisms!: SchemaMorphism[];

    _createdObjects = [] as SchemaObject[];
    _createdMorphisms = [] as SchemaMorphism[];

    private constructor() {}

    static fromServer(input: SchemaCategoryFromServer): SchemaCategory {
        const category = new SchemaCategory;
        category.id = input.id;
        category.jsonValue = input.jsonValue;
        category.objects = input.objects.map(object => SchemaObject.fromServer(object));
        //category.morphisms = input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism, category.objects));
        category.morphisms = input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism));

        return category;
    }

    createObject(label: string, keyValue: number): SchemaObject {
        const id = (this._createdObjects[this._createdObjects.length - 1]?.id ?? 0) - 1;
        const object = SchemaObject.createNew(id, label, Key.createNew(keyValue), []); // TODO
        this._createdObjects.push(object);
        return object;
    }

    createMorphism(dom: SchemaObject, cod: SchemaObject): { morphism: SchemaMorphism, dualMorphism: SchemaMorphism } {
        const id = (this._createdMorphisms[this._createdMorphisms.length - 1]?.id ?? 0) - 1;

        const morphism = SchemaMorphism.createNew(id, dom.id, cod.id);
        this._createdMorphisms.push(morphism);
        const dualMorphism = SchemaMorphism.fromDual(id - 1, morphism);
        this._createdMorphisms.push(dualMorphism);

        return { morphism, dualMorphism };
    }
}

export class SchemaCategoryFromServer {
    id!: number;
    jsonValue!: string;
    objects!: SchemaObjectFromServer[];
    morphisms!: SchemaMorphismFromServer[];
}
