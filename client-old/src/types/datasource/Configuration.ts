export type DatasourceConfigurationFromServer = {
    isPropertyToOneAllowed: boolean;
    isPropertyToManyAllowed: boolean;
    isInliningToOneAllowed: boolean;
    isInliningToManyAllowed: boolean;
    isGroupingAllowed: boolean;
    isDynamicNamingAllowed: boolean;
    isAnonymousNamingAllowed: boolean;
    isReferenceAllowed: boolean;
    isComplexPropertyAllowed: boolean;
    isSchemaless: boolean;
    isWritable: boolean;
    isQueryable: boolean;
};

export class DatasourceConfiguration {
    readonly isPropertyToOneAllowed: boolean;
    readonly isPropertyToManyAllowed: boolean;
    readonly isInliningToOneAllowed: boolean;
    readonly isInliningToManyAllowed: boolean;
    readonly isGroupingAllowed: boolean;
    readonly isDynamicNamingAllowed: boolean;
    readonly isAnonymousNamingAllowed: boolean;
    readonly isReferenceAllowed: boolean; // TODO The IC reference algorithm.
    readonly isComplexPropertyAllowed: boolean;
    readonly isSchemaless: boolean;
    readonly isWritable: boolean;
    readonly isQueryable: boolean;

    public constructor(input: DatasourceConfigurationFromServer) {
        this.isPropertyToOneAllowed = input.isPropertyToOneAllowed;
        this.isPropertyToManyAllowed = input.isPropertyToManyAllowed;
        this.isInliningToOneAllowed = input.isInliningToOneAllowed;
        this.isInliningToManyAllowed = input.isInliningToManyAllowed;
        this.isGroupingAllowed = input.isGroupingAllowed || true;
        this.isDynamicNamingAllowed = input.isDynamicNamingAllowed;
        this.isAnonymousNamingAllowed = input.isAnonymousNamingAllowed;
        this.isReferenceAllowed = input.isReferenceAllowed;
        this.isComplexPropertyAllowed = input.isComplexPropertyAllowed;
        this.isSchemaless = input.isSchemaless;
        this.isWritable = input.isWritable;
        this.isQueryable = input.isQueryable;
    }
}
