import { Key } from "../identifiers";
import { SchemaCategory } from "../schema";

interface AbstractInferenceEdit {
}
export type { AbstractInferenceEdit };


export class PrimaryKeyMergeInferenceEdit implements AbstractInferenceEdit {
    public readonly type: string = "primaryKey";
    public readonly primaryKeyRoot: Key;
    public readonly primaryKey: Key;

    constructor(primaryKeyRoot: Key, primaryKey: Key) {
        this.primaryKeyRoot = primaryKeyRoot
        this.primaryKey = primaryKey;
    }
    toJSON() {
        return {
            type: this.type,
            primaryKeyRoot: this.primaryKeyRoot,
            primaryKey: this.primaryKey
        };
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