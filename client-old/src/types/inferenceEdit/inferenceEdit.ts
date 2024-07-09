import { Key } from "../identifiers";
import { SchemaCategory } from "../schema";

interface AbstractInferenceEdit {
    applyEdit(schemaCategory: SchemaCategory): SchemaCategory;
}
export type { AbstractInferenceEdit };


export class PrimaryKeyMergeInferenceEdit implements AbstractInferenceEdit {
    public readonly primaryKey: string;

    constructor(primaryKey: string) {
        this.primaryKey = primaryKey;
    }
    applyEdit(schemaCategory: SchemaCategory): SchemaCategory {
        throw new Error("Method not implemented.");
    }
}

export class ReferenceMergeInferenceEdit implements AbstractInferenceEdit {
    public readonly referenceKey: Key;
    public readonly referredKey: Key;

    constructor(referenceKey: Key, referredKey: Key) {
        this.referenceKey = referenceKey;
        this.referredKey = referredKey;
    }
    applyEdit(schemaCategory: SchemaCategory): SchemaCategory {
        throw new Error("Method not implemented.");
    }
}