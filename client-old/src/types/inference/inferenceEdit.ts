import { Key } from '../identifiers';
import type { KeyFromServer } from '../identifiers';

export interface InferenceEdit {
    id: number | null;
    isActive: boolean;
    type: string;
}

export class PrimaryKeyMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'PrimaryKey';
    readonly primaryKey: Key;
    public isActive: boolean;
    public id: number | null;

    constructor(primaryKey: Key, isActive: boolean, id: number | null = null) {
        this.primaryKey = primaryKey;
        this.isActive = isActive;
        this.id = id;
    }
}

export class ReferenceMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'Reference';
    readonly referenceKey: Key;
    readonly referredKey: Key;
    public isActive: boolean;
    public id: number | null;

    constructor(reference: Key, referredKey: Key, isActive: boolean, id: number | null = null) {
        this.referenceKey = reference;
        this.referredKey = referredKey;
        this.isActive = isActive;
        this.id = id;
    }
}

export class ClusterInferenceEdit implements InferenceEdit {
    readonly type: string = 'Cluster';
    readonly clusterKeys: Key[];
    public isActive: boolean;
    public id: number | null;

    constructor(clusterKeys: Key[], isActive: boolean, id: number | null = null) {
        this.clusterKeys = clusterKeys;
        this.isActive = isActive;
        this.id = id;
    }
}

export class RecursionInferenceEdit implements InferenceEdit {
    readonly type: string = 'Recursion';
    readonly pattern: PatternSegment[];
    public isActive: boolean;
    public id: number | null;

    constructor(pattern: PatternSegment[], isActive: boolean, id: number | null = null) {
        this.pattern = pattern;
        this.isActive = isActive;
        this.id = id;
    }
}

export function createInferenceEditFromServer(data: any): InferenceEdit {
    switch (data.type) {
    case 'PrimaryKey':
        return new PrimaryKeyMergeInferenceEdit(
            Key.fromServer(data.primaryKey), 
            data.isActive, 
            data.id,
        );
    case 'Reference':
        return new ReferenceMergeInferenceEdit(
            Key.fromServer(data.referenceKey),
            Key.fromServer(data.referredKey),
            data.isActive,
            data.id,
        );
    case 'Cluster':
        return new ClusterInferenceEdit(
            data.clusterKeys.map((key: KeyFromServer) => Key.fromServer(key)),
            data.isActive,
            data.id,
        );
    case 'Recursion':
        return new RecursionInferenceEdit(
            data.pattern.map((segment: any) => new PatternSegment(segment.nodeName, segment.direction)),
            data.isActive,
            data.id,
        );
    default:
        throw new Error('Unknown edit type');
    }
}

export type SaveJobResultPayload = {
    isFinal: false;
    edit: InferenceEdit;
} | {
    isFinal: true;
};

export class PatternSegment {
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}
}
