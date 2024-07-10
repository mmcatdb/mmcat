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
    public readonly type: string = "reference";
    public readonly referenceKey: Key;
    public readonly referredKey: Key;

    constructor(referenceKey: Key, referredKey: Key) {
        this.referenceKey = referenceKey;
        this.referredKey = referredKey;
    }
    applyEdit(schemaCategory: SchemaCategory): SchemaCategory {
        throw new Error("Method not implemented.");
    }

    toJSON() {
        return {
            type: this.type,
            referenceKey: this.referenceKey,
            referredKey: this.referredKey
        };
    }
}

export class SaveJobResultPayload {
    constructor(
        public permanent: boolean,
        public edit: AbstractInferenceEdit
    ) {}

    toJSON() {
        return {
            permanent: this.permanent,
            edit: this.edit instanceof ReferenceMergeInferenceEdit ? this.edit.toJSON() : this.edit
        };
    }
}