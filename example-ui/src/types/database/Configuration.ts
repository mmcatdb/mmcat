export class DatabaseConfiguration {
    readonly isPropertyToOneAllowed: boolean;
    readonly isPropertyToManyAllowed: boolean;
    readonly isInliningToOneAllowed: boolean;
    readonly isInliningToManyAllowed: boolean;
    readonly isGrouppingAllowed: boolean;
    readonly isDynamicNamingAllowed: boolean;
    readonly isAnonymousNamingAllowed: boolean;
    readonly isReferenceAllowed: boolean; // TODO The IC reference algorithm.
    readonly isComplexPropertyAllowed: boolean;
    readonly isSchemaLess: boolean;

    public constructor(input: DatabaseConfigurationFromServer) {
        this.isPropertyToOneAllowed = input.isPropertyToOneAllowed;
        this.isPropertyToManyAllowed = input.isPropertyToManyAllowed;
        this.isInliningToOneAllowed = input.isInliningToOneAllowed;
        this.isInliningToManyAllowed = input.isInliningToManyAllowed;
        this.isGrouppingAllowed = input.isGrouppingAllowed;
        this.isDynamicNamingAllowed = input.isDynamicNamingAllowed;
        this.isAnonymousNamingAllowed = input.isAnonymousNamingAllowed;
        this.isReferenceAllowed = input.isReferenceAllowed;
        this.isComplexPropertyAllowed = input.isComplexPropertyAllowed;
        this.isSchemaLess = input.isSchemaLess;
    }
}

export type DatabaseConfigurationFromServer = {
    isPropertyToOneAllowed: boolean,
    isPropertyToManyAllowed: boolean,
    isInliningToOneAllowed: boolean,
    isInliningToManyAllowed: boolean,
    isGrouppingAllowed: boolean,
    isDynamicNamingAllowed: boolean,
    isAnonymousNamingAllowed: boolean,
    isReferenceAllowed: boolean,
    isComplexPropertyAllowed: boolean
    isSchemaLess: boolean;
}
