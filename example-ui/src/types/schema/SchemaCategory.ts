import { SchemaMorphism, SchemaMorphismFromServer } from "./SchemaMorphism";
import { SchemaObject, SchemaObjectFromServer } from "./SchemaObject";

export class SchemaCategory {
    public id!: number;
    public jsonValue!: string;
    public objects!: SchemaObject[];
    public morphisms!: SchemaMorphism[];

    private constructor() {}

    public static fromServer(input: SchemaCategoryFromServer): SchemaCategory {
        const category = new SchemaCategory;
        category.id = input.id;
        category.jsonValue = input.jsonValue;
        category.objects = input.objects.map(object => SchemaObject.fromServer(object));
        //category.morphisms = input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism, category.objects));
        category.morphisms = input.morphisms.map(morphism => SchemaMorphism.fromServer(morphism));

        return category;
    }
}

export class SchemaCategoryFromServer {
    public id!: number;
    public jsonValue!: string;
    public objects!: SchemaObjectFromServer[];
    public morphisms!: SchemaMorphismFromServer[];
}
