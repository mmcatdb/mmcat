
// It is stored here so that it won't be overriden by other windows.
let schemaCategoryId: number | undefined;
const schemaCategoryIdKey = "SCHEMA_CATEGORY_ID";
const schemaCategoryIdDefault = 1;

export function getSchemaCategoryId(): number {
    if (!schemaCategoryId) {
        const result = localStorage.getItem(schemaCategoryIdKey);
        const intResult = result ? parseInt(result) : null;

        schemaCategoryId = intResult || schemaCategoryIdDefault;
    }

    return schemaCategoryId;
}

export function setSchemaCategoryId(value: number): void {
    schemaCategoryId = value;
    localStorage.setItem(schemaCategoryIdKey, '' + value);
}
