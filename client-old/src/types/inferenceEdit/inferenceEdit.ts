import { Key } from "../identifiers";

interface AbstractInferenceEdit {
    id: number | undefined;
    isActive: boolean;
    toJSON(): any;
}
export type { AbstractInferenceEdit };

export class PrimaryKeyMergeInferenceEdit implements AbstractInferenceEdit {
    public readonly type: string = "primaryKey";
    public readonly primaryKey: Key;
    public isActive: boolean;
    public id: number | undefined;

    constructor(primaryKey: Key, isActive: boolean, id?: number) {
        this.primaryKey = primaryKey;
        this.isActive = isActive;
        this.id = id;
    }
    toJSON() {
        return {
            type: this.type,
            primaryKey: this.primaryKey,
            isActive: this.isActive,
            id: this.id,
        };
    }
}

export class ReferenceMergeInferenceEdit implements AbstractInferenceEdit {
    public readonly type: string = "reference";
    public readonly referenceKey: Key;
    public readonly referredKey: Key;
    public isActive: boolean;
    public id: number | undefined;

    constructor(referenceKey: Key, referredKey: Key, isActive: boolean, id?: number) {
        this.referenceKey = referenceKey;
        this.referredKey = referredKey;
        this.isActive = isActive;
        this.id = id;
    }

    toJSON() {
        return {
            type: this.type,
            referenceKey: this.referenceKey,
            referredKey: this.referredKey,
            isActive: this.isActive,
            id: this.id,
        };
    }
}

export class ClusterInferenceEdit implements AbstractInferenceEdit {
    public readonly type: string = "cluster";
    public readonly clusterKeys: Key[];
    public isActive: boolean;
    public id: number | undefined;

    constructor(clusterKeys: Key[], isActive: boolean, id?: number) {
        this.clusterKeys = clusterKeys;
        this.isActive = isActive;
        this.id = id;
    }

    toJSON() {
        return {
            type: this.type,
            clusterKeys: this.clusterKeys,
            isActive: this.isActive,
            id: this.id,
        };
    }
}

export class RecursionInferenceEdit implements AbstractInferenceEdit {
    public readonly type: string = "recursion";
    public readonly pattern: PatternSegment[];
    public isActive: boolean;
    public id: number | undefined;

    constructor(pattern: PatternSegment[], isActive: boolean, id?: number) {
        this.pattern = pattern;
        this.isActive = isActive;
        this.id = id;
    }

    toJSON() {
        return {
            type: this.type,
            pattern: this.pattern.map(segment => ({
                nodeName: segment.nodeName,
                direction: segment.direction,
                isActive: this.isActive,
                id: this.id,
            }))
        };
    }
}

export function createInferenceEditFromServer(data: any): AbstractInferenceEdit {
    switch (data.type) {
    case "primaryKey":
        return new PrimaryKeyMergeInferenceEdit(
            Key.fromServer(data.primaryKey), 
            data.isActive, 
            data.id
        );
    case "reference":
        return new ReferenceMergeInferenceEdit(
            Key.fromServer(data.referenceKey),
            Key.fromServer(data.referredKey),
            data.isActive,
            data.id
        );
    case "cluster":
        return new ClusterInferenceEdit(
            data.clusterKeys.map((key: number) => Key.fromServer(key)),
            data.isActive,
            data.id
        );
    case "recursion":
        return new RecursionInferenceEdit(
            data.pattern.map((segment: any) => new PatternSegment(segment.nodeName, segment.direction)),
            data.isActive,
            data.id
        );
    default:
        throw new Error("Unknown edit type");
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

export class PatternSegment {
    public readonly nodeName : string;
    public readonly direction: string;

    constructor (nodeName: string, direction: string) {
        this.nodeName = nodeName;
        this.direction = direction;
    }
}
