// Unfortunatelly, this does not work well with the vue components, because their props expect "object" instead of "string"
// export type Id = SpecialType<string, 'Id'>;
export type Id = string;

export interface Entity {
    id: Id;
}

// Unfortunatelly, this does not work well with the vue components, because their props expect "object" instead of "string"
// export type VersionId = UniqueType<string, 'version'>;
export type VersionId = string;
