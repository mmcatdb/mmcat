/**
 * Class representing a collection of primary key and reference candidates.
 */
export class Candidates {
    /**
     * @param {PrimaryKeyCandidate[]} pkCandidates - Array of primary key candidates.
     * @param {ReferenceCandidate[]} refCandidates - Array of reference candidates.
     */
    constructor(
        public pkCandidates: PrimaryKeyCandidate[],
        public refCandidates: ReferenceCandidate[],
    ) {}

    /**
     * Creates an instance of `Candidates` from a serialized input.
     * @param {SerializedCandidates} input - The serialized candidates data from the server.
     * @returns {Candidates} - The deserialized `Candidates` object.
     */
    static fromServer(input: SerializedCandidates): Candidates {
        const pkCandidates = input.pkCandidates.map(PrimaryKeyCandidate.fromServer);
        const refCandidates = input.refCandidates.map(ReferenceCandidate.fromServer);

        return new Candidates(pkCandidates, refCandidates);
    }
}

/**
 * Type representing the structure of serialized candidates received from the server.
 * @typedef {Object} SerializedCandidates
 * @property {SerializedPrimaryKeyCandidate[]} pkCandidates - Serialized array of primary key candidates.
 * @property {SerializedReferenceCandidate[]} refCandidates - Serialized array of reference candidates.
 */
export type SerializedCandidates = {
    pkCandidates: SerializedPrimaryKeyCandidate[];
    refCandidates: SerializedReferenceCandidate[];
};

/**
 * Type representing a serialized primary key candidate.
 * @typedef {Object} SerializedPrimaryKeyCandidate
 * @property {string} type - The type of the primary key candidate.
 * @property {string} hierarchicalName - The hierarchical name of the candidate.
 * @property {boolean} selected - Whether the candidate is selected.
 */
export type SerializedPrimaryKeyCandidate = {
    type: string;
    hierarchicalName: string;
    selected: boolean;
};

/**
 * Type representing a serialized reference candidate.
 * @typedef {Object} SerializedReferenceCandidate
 * @property {string} type - The type of the reference candidate.
 * @property {string} referred - The referred object name.
 * @property {string} referencing - The referencing object name.
 * @property {boolean} weak - Whether the reference is weak.
 * @property {boolean} selected - Whether the candidate is selected.
 */
export type SerializedReferenceCandidate = {
    type: string;
    referred: string;
    referencing: string;
    weak: boolean;
    selected: boolean;
};

/**
 * Class representing a primary key candidate.
 */
export class PrimaryKeyCandidate {
    /**
     * @param {string} type - The type of the primary key candidate.
     * @param {string} hierarchicalName - The hierarchical name of the candidate.
     * @param {boolean} selected - Whether the candidate is selected.
     */
    constructor(
        public type: string,
        public hierarchicalName: string,
        public selected: boolean,
    ) {}

    /**
     * Creates an instance of `PrimaryKeyCandidate` from a serialized input.
     * @param {SerializedPrimaryKeyCandidate} input - The serialized primary key candidate data from the server.
     * @returns {PrimaryKeyCandidate} - The deserialized `PrimaryKeyCandidate` object.
     */
    static fromServer(input: SerializedPrimaryKeyCandidate): PrimaryKeyCandidate {
        return new PrimaryKeyCandidate(
            input.type,
            input.hierarchicalName,
            input.selected,
        );
    }
}

/**
 * Class representing a reference candidate.
 */
export class ReferenceCandidate {
    /**
     * @param {string} type - The type of the reference candidate.
     * @param {string} referred - The referred object name.
     * @param {string} referencing - The referencing object name.
     * @param {boolean} weak - Whether the reference is weak.
     * @param {boolean} selected - Whether the candidate is selected.
     */
    constructor(
        public type: string,
        public referred: string,
        public referencing: string,
        public weak: boolean,
        public selected: boolean,
    ) {}

    /**
     * Creates an instance of `ReferenceCandidate` from a serialized input.
     * @param {SerializedReferenceCandidate} input - The serialized reference candidate data from the server.
     * @returns {ReferenceCandidate} - The deserialized `ReferenceCandidate` object.
     */
    static fromServer(input: SerializedReferenceCandidate): ReferenceCandidate {
        return new ReferenceCandidate(
            input.type,
            input.referred,
            input.referencing,
            input.weak,
            input.selected,
        );
    }
}
