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
