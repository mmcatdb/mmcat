export class DatabaseConfiguration {
    public readonly isRootObjectAllowed: boolean;
    public readonly isPropertyToOneAllowed: boolean;
    public readonly isPropertyToManyAllowed: boolean;
    public readonly isInliningToOneAllowed: boolean;
    public readonly isInliningToManyAllowed: boolean;
    public readonly isGrouppingAllowed: boolean;
    public readonly isDynamicNamingAllowed: boolean;
    public readonly isAnonymousNamingAllowed: boolean;
    public readonly isReferenceAllowed: boolean;

    public constructor(fromServer: DatabaseConfigurationFromServer) {
        this.isRootObjectAllowed = fromServer.isRootObjectAllowed;
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
    isPropertyToOneAllowed: boolean,
    isPropertyToManyAllowed: boolean,
    isInliningToOneAllowed: boolean,
    isInliningToManyAllowed: boolean,
    isGrouppingAllowed: boolean,
    isDynamicNamingAllowed: boolean,
    isAnonymousNamingAllowed: boolean,
    isReferenceAllowed: boolean
}

export const TEST_CONFIGURATION = {
    isRootObjectAllowed: true,
    isPropertyToOneAllowed: true,
    //isPropertyToManyAllowed: true,
    isPropertyToManyAllowed: false,
    isInliningToOneAllowed: true,
    //isInliningToManyAllowed: true,
    isInliningToManyAllowed: false,
    isGrouppingAllowed: true,
    isDynamicNamingAllowed: true,
    isAnonymousNamingAllowed: true,
    isReferenceAllowed: true
};
