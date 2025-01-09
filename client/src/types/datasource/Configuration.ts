export type DatasourceConfigurationFromServer = {
    isPropertyToOneAllowed: boolean;
    isPropertyToManyAllowed: boolean;
    isInliningToOneAllowed: boolean;
    isInliningToManyAllowed: boolean;
    isGroupingAllowed: boolean;
    isAnonymousNamingAllowed: boolean;
    isReferenceAllowed: boolean;
    isComplexPropertyAllowed: boolean;
    isSchemaless: boolean;
};

export class DatasourceConfiguration {
    readonly isPropertyToOneAllowed: boolean;
    readonly isPropertyToManyAllowed: boolean;
    readonly isInliningToOneAllowed: boolean;
    readonly isInliningToManyAllowed: boolean;
    readonly isGroupingAllowed: boolean;
    readonly isAnonymousNamingAllowed: boolean;
    readonly isReferenceAllowed: boolean; // TODO The IC reference algorithm.
    readonly isComplexPropertyAllowed: boolean;
    readonly isSchemaless: boolean;

    constructor(input: DatasourceConfigurationFromServer) {
        this.isPropertyToOneAllowed = input.isPropertyToOneAllowed;
        this.isPropertyToManyAllowed = input.isPropertyToManyAllowed;
        this.isInliningToOneAllowed = input.isInliningToOneAllowed;
        this.isInliningToManyAllowed = input.isInliningToManyAllowed;
        this.isGroupingAllowed = input.isGroupingAllowed || true;
        this.isAnonymousNamingAllowed = input.isAnonymousNamingAllowed;
        this.isReferenceAllowed = input.isReferenceAllowed;
        this.isComplexPropertyAllowed = input.isComplexPropertyAllowed;
        this.isSchemaless = input.isSchemaless;
    }
}
