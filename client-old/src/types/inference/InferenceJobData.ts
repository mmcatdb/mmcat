import type { JobDataType } from '../job';
import { SchemaCategory, type SchemaCategoryInfo, type SerializedSchema, type SerializedMetadata } from '../schema';
import { Candidates, type SerializedCandidates } from './candidates';
import type { InferenceEdit, SerializedInferenceEdit } from './inferenceEdit';
import { createInferenceEditFromServer } from './inferenceEdit';
import type { LayoutType } from './layoutType';

/**
 * Type representing the data structure of an inference job received from the server.
 */
export type InferenceJobDataFromServer = {
    /** The type of job, in this case, an inference job. */
    type: JobDataType.Inference;
    /** The serialized inference edits associated with the job. */
    edits: SerializedInferenceEdit[];
    /** The serialized inference schema. */
    inferenceSchema: SerializedSchema;
    /** The serialized final schema. */
    finalSchema: SerializedSchema;
    /** Metadata related to the inference schema. */
    inferenceMetadata: SerializedMetadata;
    /** Metadata related to the final schema. */
    finalMetadata: SerializedMetadata;
    /** The layout type used for visualizing the data. */
    layoutType: LayoutType;
    /** Serialized candidates data for the job. */
    candidates: SerializedCandidates;
};

/**
 * Class representing the data for an inference job.
 */
export class InferenceJobData {
    constructor(
        public edits: InferenceEdit[],
        public inferenceSchema: SchemaCategory,
        public finalSchema: SchemaCategory,
        public layoutType: LayoutType,
        public candidates: Candidates,
    ) {}

    /**
     * Creates an instance of `InferenceJobData` from server data.
     */
    static fromServer(input: InferenceJobDataFromServer, info: SchemaCategoryInfo): InferenceJobData {
        return new InferenceJobData(
            input.edits.map(createInferenceEditFromServer),
            SchemaCategory.fromServerWithInfo(info, input.inferenceSchema, input.inferenceMetadata),
            SchemaCategory.fromServerWithInfo(info, input.finalSchema, input.finalMetadata),
            input.layoutType,
            Candidates.fromServer(input.candidates),
        );
    }
}
