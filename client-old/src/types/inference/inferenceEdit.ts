import { Key } from '../identifiers';
import type { KeyFromServer } from '../identifiers';
import { PrimaryKeyCandidate, ReferenceCandidate } from './candidates';
import type { LayoutType } from '@/types/inference/layoutType';
import type { SerializedPrimaryKeyCandidate, SerializedReferenceCandidate } from './candidates';
import type { Position } from 'cytoscape';

/**
 * Type representing an inference edit.
 */
export type InferenceEdit = {
    id: number | null;
    isActive: boolean;
    type: string;
};

/**
 * Creates an inference edit from serialized server data.
 */
export function createInferenceEditFromServer(data: SerializedInferenceEdit): InferenceEdit {
    switch (data.type) {
    case 'PrimaryKey':
        return PrimaryKeyMergeInferenceEdit.fromServer(data);
    case 'Reference':
        return ReferenceMergeInferenceEdit.fromServer(data);
    case 'Cluster':
        return ClusterInferenceEdit.fromServer(data);
    case 'Recursion':
        return RecursionInferenceEdit.fromServer(data);
    default:
        throw new Error('Unknown edit type');
    }
}

/**
 * Type representing serialized inference edit data received from the server.
 */
export type SerializedInferenceEdit = {
    /** The ID of the edit. */
    id: number | null;
    /** Whether the edit is active. */
    isActive: boolean;
    /** The type of the edit. */
    type: 'PrimaryKey' | 'Reference' | 'Cluster' | 'Recursion';
    /** The primary key involved in the edit (if applicable). */
    primaryKey?: KeyFromServer;
    /** The identified primary key (if applicable). */
    primaryKeyIdentified?: KeyFromServer;
    /** The candidate for the edit (if applicable). */
    candidate?: SerializedPrimaryKeyCandidate | SerializedReferenceCandidate;
    /** The reference key (if applicable). */
    referencingKey?: KeyFromServer;
    /** The referred key (if applicable). */
    referencedKey?: KeyFromServer;
    /** The cluster keys involved (if applicable). */
    clusterKeys?: KeyFromServer[];
    /** The recursion pattern (if applicable). */
    pattern?: SerializedPatternSegment[];
};

/**
 * Class representing a primary key merge inference edit.
 */
export class PrimaryKeyMergeInferenceEdit {
    readonly type: string = 'PrimaryKey';
    readonly primaryKey?: Key;
    readonly primaryKeyIdentified?: Key;
    readonly candidate?: PrimaryKeyCandidate;
    public isActive: boolean;
    public id: number | null;

    constructor(primaryKeyOrCandidate: Key | PrimaryKeyCandidate, primaryKeyIdentifiedOrIsActive: Key | boolean, isActiveOrId?: boolean | number | null, id: number | null = null) {
        if (primaryKeyOrCandidate instanceof Key && primaryKeyIdentifiedOrIsActive instanceof Key) {
            this.primaryKey = primaryKeyOrCandidate;
            this.primaryKeyIdentified = primaryKeyIdentifiedOrIsActive;
            this.isActive = isActiveOrId as boolean;
            this.id = id ?? null;
        }
        else if (primaryKeyOrCandidate instanceof PrimaryKeyCandidate) {
            this.candidate = primaryKeyOrCandidate;
            this.isActive = primaryKeyIdentifiedOrIsActive as boolean;
            this.id = isActiveOrId as number ?? null;
        }
        else {
            throw new Error('Invalid constructor arguments for PrimaryKeyMergeInferenceEdit.');
        }
    }

    static fromServer(data: SerializedInferenceEdit): PrimaryKeyMergeInferenceEdit {
        if (data.primaryKey != null && data.primaryKeyIdentified != null) {
            return new PrimaryKeyMergeInferenceEdit(
                Key.fromServer(data.primaryKey),
                Key.fromServer(data.primaryKeyIdentified),
                data.isActive,
                data.id,
            );
        }
        else if (data.candidate != null) {
            return new PrimaryKeyMergeInferenceEdit(
                PrimaryKeyCandidate.fromServer(data.candidate as SerializedPrimaryKeyCandidate),
                data.isActive,
                data.id,
            );
        }
        throw new Error('Invalid server data for PrimaryKeyMergeInferenceEdit');
    }
}

/**
 * Class representing a reference merge inference edit.
 */
export class ReferenceMergeInferenceEdit {
    readonly type: string = 'Reference';
    readonly referencingKey?: Key;
    readonly referencedKey?: Key;
    readonly candidate?: ReferenceCandidate;
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a {@link ReferenceMergeInferenceEdit} from either keys or a candidate.
     * @param referenceOrCandidate - The reference key or candidate involved in the edit.
     * @param referencedKeyOrIsActive - The referred key or whether the edit is active.
     * @param isActiveOrId - Whether the edit is active or the edit ID.
     * @param id - The ID of the edit.
     */
    constructor(referenceOrCandidate: Key | ReferenceCandidate, referencedKeyOrIsActive: Key | boolean, isActiveOrId?: boolean | number | null, id?: number | null) {
        if (referenceOrCandidate instanceof Key && referencedKeyOrIsActive instanceof Key) {
            this.referencingKey = referenceOrCandidate;
            this.referencedKey = referencedKeyOrIsActive;
            this.isActive = isActiveOrId as boolean;
            this.id = id ?? null;
        }
        else if (referenceOrCandidate instanceof ReferenceCandidate) {
            this.candidate = referenceOrCandidate;
            this.isActive = referencedKeyOrIsActive as boolean;
            this.id = isActiveOrId as number ?? null;
        }
        else {
            throw new Error('Invalid constructor arguments for ReferenceMergeInferenceEdit.');
        }
    }

    static fromServer(data: SerializedInferenceEdit): ReferenceMergeInferenceEdit {
        if (data.referencingKey != null && data.referencedKey != null) {
            return new ReferenceMergeInferenceEdit(
                Key.fromServer(data.referencingKey),
                Key.fromServer(data.referencedKey),
                data.isActive,
                data.id,
            );
        }
        else if (data.candidate != null) {
            return new ReferenceMergeInferenceEdit(
                ReferenceCandidate.fromServer(data.candidate as SerializedReferenceCandidate),
                data.isActive,
                data.id,
            );
        }
        throw new Error('Invalid server data for ReferenceMergeInferenceEdit');
    }
}

/**
 * Class representing a cluster inference edit.
 */
export class ClusterInferenceEdit {
    readonly type: string = 'Cluster';
    readonly clusterKeys: Key[];
    public isActive: boolean;
    public id: number | null;

    constructor(clusterKeys: Key[], isActive: boolean, id: number | null = null) {
        this.clusterKeys = clusterKeys;
        this.isActive = isActive;
        this.id = id;
    }

    static fromServer(data: SerializedInferenceEdit): ClusterInferenceEdit {
        return new ClusterInferenceEdit(
            data.clusterKeys!.map(Key.fromServer),
            data.isActive,
            data.id,
        );
    }
}

/**
 * Class representing a recursion inference edit.
 */
export class RecursionInferenceEdit {
    readonly type: string = 'Recursion';
    readonly pattern: PatternSegment[];
    public isActive: boolean;
    public id: number | null;

    constructor(pattern: PatternSegment[], isActive: boolean, id: number | null = null) {
        this.pattern = pattern;
        this.isActive = isActive;
        this.id = id;
    }

    static fromServer(data: SerializedInferenceEdit): RecursionInferenceEdit {
        return new RecursionInferenceEdit(
            data.pattern!.map(segment => new PatternSegment(segment.nodeName, segment.direction)),
            data.isActive,
            data.id,
        );
    }
}

/**
 * Class representing a segment of a recursion pattern.
 */
export class PatternSegment {
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}

    static fromServer(data: SerializedPatternSegment): PatternSegment {
        return new PatternSegment(data.nodeName, data.direction);
    }
}

/**
 * Type representing a serialized recursion pattern segment.
 */
export type SerializedPatternSegment = {
    /** The name of the node in the pattern. */
    nodeName: string;
    /** The direction of the recursion. */
    direction: string;
};

/**
 * Type representing the payload when saving a job result.
 */
export type SaveJobResultPayload = {
    /** Whether the job result is final. */
    isFinal: boolean | null;
    /** The associated inference edit, if any. */
    edit: InferenceEdit | null;
    /** The layout type used. */
    layoutType: LayoutType | null;
    /** An array of key-position pairs representing updated positions. */
    positions: { key: Key, position: Position }[] | null;
};
