export class DatabaseConfiguration {
    readonly isRootObjectAllowed: boolean; // TODO
    readonly isPropertyToOneAllowed: boolean;
    readonly isPropertyToManyAllowed: boolean;
    readonly isInliningToOneAllowed: boolean;
    readonly isInliningToManyAllowed: boolean;
    readonly isGrouppingAllowed: boolean;
    readonly isDynamicNamingAllowed: boolean;
    readonly isAnonymousNamingAllowed: boolean;
    readonly isReferenceAllowed: boolean; // TODO
    readonly isComplexPropertyAllowed: boolean; // TODO upraveno add a edit property, ale ještě podle toho nejsou povoleny či zakázány nody při vybírání signatury

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
        this.isComplexPropertyAllowed = fromServer.isComplexPropertyAllowed;
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
    isReferenceAllowed: boolean,
    isComplexPropertyAllowed: boolean
}
