import type { JobDataType } from '../job';
import { SchemaCategory, type SchemaCategoryInfo, type SerializedSchema, type SerializedMetadata } from '../schema';
import { Candidates, type SerializedCandidates } from './candidates';
import type { InferenceEdit } from './inferenceEdit';

export type InferenceJobDataFromServer = {
    type: JobDataType.Inference;
    edits: InferenceEdit[];
    inferenceSchema: SerializedSchema;
    finalSchema: SerializedSchema;
    inferenceMetadata: SerializedMetadata;
    finalMetadata: SerializedMetadata;
    candidates: SerializedCandidates;
    // mappings
};

export class InferenceJobData {
    constructor(
        public edits: InferenceEdit[],
        public inferenceSchema: SchemaCategory,
        public finalSchema: SchemaCategory,
        public candidates: Candidates,
    ) {}

    static fromServer(input: InferenceJobDataFromServer, info: SchemaCategoryInfo): InferenceJobData {
        console.log('edits in InferenceJobData', input.edits);
        return new InferenceJobData(
            input.edits,
            //input.edits.map(createInferenceEditFromServer),
            SchemaCategory.fromServerWithInfo(info, input.inferenceSchema, input.inferenceMetadata),
            SchemaCategory.fromServerWithInfo(info, input.finalSchema, input.finalMetadata),
            Candidates.fromServer(input.candidates),
        );
    }
}
