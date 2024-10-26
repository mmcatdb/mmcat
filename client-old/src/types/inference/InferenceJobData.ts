import type { JobDataType } from '../job';
import { SchemaCategory, type SchemaCategoryInfo, type SerializedSchema, type SerializedMetadata } from '../schema';
import { Candidates, type SerializedCandidates } from './candidates';
import type { InferenceEdit, SerializedInferenceEdit } from './inferenceEdit';
import { createInferenceEditFromServer } from './inferenceEdit';
import type { LayoutType } from './layoutType';

/**
 * Type representing the data structure of an inference job received from the server.
 * @typedef {Object} InferenceJobDataFromServer
 * @property {JobDataType.Inference} type - The type of job, in this case, an inference job.
 * @property {SerializedInferenceEdit[]} edits - The serialized inference edits associated with the job.
 * @property {SerializedSchema} inferenceSchema - The serialized inference schema.
 * @property {SerializedSchema} finalSchema - The serialized final schema.
 * @property {SerializedMetadata} inferenceMetadata - Metadata related to the inference schema.
 * @property {SerializedMetadata} finalMetadata - Metadata related to the final schema.
 * @property {LayoutType} layoutType - The layout type used for visualizing the data.
 * @property {SerializedCandidates} candidates - Serialized candidates data for the job.
 */
export type InferenceJobDataFromServer = {
    type: JobDataType.Inference;
    edits: SerializedInferenceEdit[];
    inferenceSchema: SerializedSchema;
    finalSchema: SerializedSchema;
    inferenceMetadata: SerializedMetadata;
    finalMetadata: SerializedMetadata;
    layoutType: LayoutType;
    candidates: SerializedCandidates;
};

/**
 * Class representing the data for an inference job.
 */
export class InferenceJobData {
    /**
     * Constructs an `InferenceJobData` instance.
     * @param {InferenceEdit[]} edits - The list of inference edits.
     * @param {SchemaCategory} inferenceSchema - The schema used during inference.
     * @param {SchemaCategory} finalSchema - The final schema after inference.
     * @param {LayoutType} layoutType - The layout type used in the visualization.
     * @param {Candidates} candidates - The candidates involved in the job.
     */
    constructor(
        public edits: InferenceEdit[],
        public inferenceSchema: SchemaCategory,
        public finalSchema: SchemaCategory,
        public layoutType: LayoutType,
        public candidates: Candidates,
    ) {}

    /**
     * Creates an instance of `InferenceJobData` from server data.
     * @param {InferenceJobDataFromServer} input - The serialized job data from the server.
     * @param {SchemaCategoryInfo} info - Information about the schema category.
     * @returns {InferenceJobData} - A new instance of `InferenceJobData`.
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
