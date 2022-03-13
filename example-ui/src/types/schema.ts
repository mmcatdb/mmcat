/*
export class SchemaObject {
    public key: number | undefined;
    public label: number | undefined;

    public static fromServer(input: any): SchemaObject {
        const object = new SchemaObject();

        object.key = input.key.value;
        object.label = input.label;

        return object;
    }
}

export class SchemaObjectFromServer {

}

export class SchemaMorphism {
    public domKey: number | undefined;
    public codKey: number | undefined;

    public static fromServer(input: any): SchemaMorphism {
        const morphism = new SchemaMorphism();

        morphism.domKey = input.domIdentifier.value;
        morphism.codKey = input.codIdentifier.value;

        return morphism;
    }
}
*/

export class SchemaObject {
    //public key: number | undefined;
    //public label: number | undefined;

    public id!: number;
    public jsonValue!: string;

    private constructor() {};

    public static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        //object.key = input.key.value;
        //object.label = input.label;
        object.id = input.id;
        object.jsonValue = input.jsonValue;

        return object;
    }
}

export class SchemaObjectFromServer {
    public id!: number;
    public jsonValue!: string;
}

export class SchemaMorphism {
    //public domKey: number | undefined;
    //public codKey: number | undefined;
    public id!: number;
    public domId!: number;
    public codId!: number;
    public jsonValue!: string;

    public static fromServer(input: SchemaMorphism): SchemaMorphism {
        const morphism = new SchemaMorphism();

        //morphism.domKey = input.domIdentifier.value;
        //morphism.codKey = input.codIdentifier.value;

        morphism.id = input.id;
        morphism.domId = input.domId;
        morphism.codId = input.codId;
        morphism.jsonValue = input.jsonValue;

        return morphism;
    }
}

export class SchemaMorphismFromServer {
    public id!: number;
    public domId!: number;
    public codId!: number;
    public jsonValue!: string;
}

export class SchemaCategory {
    public id!: number;
    public jsonValue!: string;
    public objects!: SchemaObject[];
    public morphisms!: SchemaMorphism[];

    private constructor() {};

    public static fromServer(input: SchemaCategoryFromServer): SchemaCategory {

        const category = new SchemaCategory;
        category.id = input.id;
        category.jsonValue = input.jsonValue;
        category.objects = input.objects.map(object => SchemaObject.fromServer(object));
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
