import { Key } from '../identifiers';
import type { KeyFromServer } from '../identifiers';
import { PrimaryKeyCandidate, ReferenceCandidate } from './candidates';
import type { LayoutType } from '@/types/inference/layoutType';
import type { SerializedPrimaryKeyCandidate, SerializedReferenceCandidate } from './candidates';

/**
 * Type representing an inference edit.
 */
export type InferenceEdit = {
    id: number | null;
    isActive: boolean;
    type: string;
};

/**
 * Creates an inference edit object from serialized server data.
 */
export function createInferenceEditFromServer(data: string): InferenceEdit {
    // FIXME Use default json mapping instead of manual parsing.
    const parsedData = JSON.parse(data) as SerializedInferenceEdit;
    switch (parsedData.type) {
    case 'PrimaryKey':
        return PrimaryKeyMergeInferenceEdit.fromServer(parsedData);
    case 'Reference':
        return ReferenceMergeInferenceEdit.fromServer(parsedData);
    case 'Cluster':
        return ClusterInferenceEdit.fromServer(parsedData);
    case 'Recursion':
        return RecursionInferenceEdit.fromServer(parsedData);
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
    referenceKey?: KeyFromServer;
    /** The referred key (if applicable). */
    referredKey?: KeyFromServer;
    /** The cluster keys involved (if applicable). */
    clusterKeys?: KeyFromServer[];
    /** The recursion pattern (if applicable). */
    pattern?: SerializedPatternSegment[];
};

/**
 * Class representing a primary key merge inference edit.
 * @implements {InferenceEdit}
 */
export class PrimaryKeyMergeInferenceEdit {
    readonly type: string = 'PrimaryKey';
    readonly primaryKey?: Key;
    readonly primaryKeyIdentified?: Key;
    readonly candidate?: PrimaryKeyCandidate;
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `PrimaryKeyMergeInferenceEdit` from either keys or a candidate.
     */
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

    /**
     * Creates a `PrimaryKeyMergeInferenceEdit` instance from serialized server data.
     */
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
 * @implements {InferenceEdit}
 */
export class ReferenceMergeInferenceEdit {
    readonly type: string = 'Reference';
    readonly referenceKey?: Key;
    readonly referredKey?: Key;
    readonly candidate?: ReferenceCandidate;
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `ReferenceMergeInferenceEdit` from either keys or a candidate.
     * @param {Key | ReferenceCandidate} referenceOrCandidate - The reference key or candidate involved in the edit.
     * @param {Key | boolean} referredKeyOrIsActive - The referred key or whether the edit is active.
     * @param {boolean | number | null} [isActiveOrId] - Whether the edit is active or the edit ID.
     * @param {number | null} [id=null] - The ID of the edit.
     */
    constructor(referenceOrCandidate: Key | ReferenceCandidate, referredKeyOrIsActive: Key | boolean, isActiveOrId?: boolean | number | null, id?: number | null) {
        if (referenceOrCandidate instanceof Key && referredKeyOrIsActive instanceof Key) {
            this.referenceKey = referenceOrCandidate;
            this.referredKey = referredKeyOrIsActive;
            this.isActive = isActiveOrId as boolean;
            this.id = id ?? null;
        }
        else if (referenceOrCandidate instanceof ReferenceCandidate) {
            this.candidate = referenceOrCandidate;
            this.isActive = referredKeyOrIsActive as boolean;
            this.id = isActiveOrId as number ?? null;
        }
        else {
            throw new Error('Invalid constructor arguments for ReferenceMergeInferenceEdit.');
        }
    }

    /**
     * Creates a `ReferenceMergeInferenceEdit` instance from serialized server data.
     * @param {SerializedInferenceEdit} data - The serialized data from the server.
     * @returns {ReferenceMergeInferenceEdit} - The deserialized `ReferenceMergeInferenceEdit` object.
     */
    static fromServer(data: SerializedInferenceEdit): ReferenceMergeInferenceEdit {
        if (data.referenceKey != null && data.referredKey != null) {
            return new ReferenceMergeInferenceEdit(
                Key.fromServer(data.referenceKey),
                Key.fromServer(data.referredKey),
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
 * @implements {InferenceEdit}
 */
export class ClusterInferenceEdit {
    readonly type: string = 'Cluster';
    readonly clusterKeys: Key[];
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `ClusterInferenceEdit`.
     */
    constructor(clusterKeys: Key[], isActive: boolean, id: number | null = null) {
        this.clusterKeys = clusterKeys;
        this.isActive = isActive;
        this.id = id;
    }

    /**
     * Creates a `ClusterInferenceEdit` instance from serialized server data.
     */
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
 * @implements {InferenceEdit}
 */
export class RecursionInferenceEdit {
    readonly type: string = 'Recursion';
    readonly pattern: PatternSegment[];
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `RecursionInferenceEdit`.
     */
    constructor(pattern: PatternSegment[], isActive: boolean, id: number | null = null) {
        this.pattern = pattern;
        this.isActive = isActive;
        this.id = id;
    }

    /**
     * Creates a `RecursionInferenceEdit` instance from serialized server data.
     */
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
    /**
     * Constructs a `PatternSegment`.
     */
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}

    /**
     * Creates a `PatternSegment` instance from serialized server data.
     */
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
};
