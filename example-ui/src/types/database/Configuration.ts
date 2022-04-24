export class DatabaseConfiguration {
    public readonly isRootObjectAllowed: boolean; // TODO
    public readonly isRootMorphismAllowed: boolean; // TODO
    public readonly isPropertyToOneAllowed: boolean;
    public readonly isPropertyToManyAllowed: boolean;
    public readonly isInliningToOneAllowed: boolean;
    public readonly isInliningToManyAllowed: boolean;
    public readonly isGrouppingAllowed: boolean; // TODO
    public readonly isDynamicNamingAllowed: boolean;
    public readonly isAnonymousNamingAllowed: boolean;
    public readonly isReferenceAllowed: boolean; // TODO

    public constructor(fromServer: DatabaseConfigurationFromServer) {
        this.isRootObjectAllowed = fromServer.isRootObjectAllowed;
        this.isRootMorphismAllowed = fromServer.isRootMorphismAllowed;
        this.isPropertyToOneAllowed = fromServer.isPropertyToOneAllowed;
        this.isPropertyToManyAllowed = fromServer.isPropertyToManyAllowed;
        this.isInliningToOneAllowed = fromServer.isInliningToOneAllowed;
        this.isInliningToManyAllowed = fromServer.isInliningToManyAllowed;
        this.isGrouppingAllowed = fromServer.isGrouppingAllowed;
        this.isDynamicNamingAllowed = fromServer.isDynamicNamingAllowed;
        this.isAnonymousNamingAllowed = fromServer.isAnonymousNamingAllowed;
        this.isReferenceAllowed = fromServer.isReferenceAllowed;
    }
}

export type DatabaseConfigurationFromServer = {
    isRootObjectAllowed: boolean,
    isRootMorphismAllowed: boolean,
    isPropertyToOneAllowed: boolean,
    isPropertyToManyAllowed: boolean,
    isInliningToOneAllowed: boolean,
    isInliningToManyAllowed: boolean,
    isGrouppingAllowed: boolean,
    isDynamicNamingAllowed: boolean,
    isAnonymousNamingAllowed: boolean,
    isReferenceAllowed: boolean
}
