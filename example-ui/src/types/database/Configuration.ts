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

    public constructor(fromServer: DatabaseConfigurationFromServer) {
        this.isPropertyToOneAllowed = fromServer.isPropertyToOneAllowed;
        this.isPropertyToManyAllowed = fromServer.isPropertyToManyAllowed;
        this.isInliningToOneAllowed = fromServer.isInliningToOneAllowed;
        this.isInliningToManyAllowed = fromServer.isInliningToManyAllowed;
        this.isGrouppingAllowed = fromServer.isGrouppingAllowed;
        this.isDynamicNamingAllowed = fromServer.isDynamicNamingAllowed;
        this.isAnonymousNamingAllowed = fromServer.isAnonymousNamingAllowed;
        this.isReferenceAllowed = fromServer.isReferenceAllowed;
        this.isComplexPropertyAllowed = fromServer.isComplexPropertyAllowed;
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
}
