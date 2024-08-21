import type { JobDataType } from '../job';
import { SchemaCategory, type SchemaCategoryInfo, type SerializedSchema, type SerializedMetadata } from '../schema';
import { Candidates, type SerializedCandidates } from './candidates';
import { type InferenceEdit, type SerializedInferenceEdit } from './inferenceEdit';
import { createInferenceEditFromServer } from './inferenceEdit';
import { LayoutType } from './layoutType';

export type InferenceJobDataFromServer = {
    type: JobDataType.Inference;
    edits: SerializedInferenceEdit[];
    inferenceSchema: SerializedSchema;
    finalSchema: SerializedSchema;
    inferenceMetadata: SerializedMetadata;
    finalMetadata: SerializedMetadata;
    layoutType: LayoutType;
    candidates: SerializedCandidates;
    // mappings
};

export class InferenceJobData {
    constructor(
        public edits: InferenceEdit[],
        public inferenceSchema: SchemaCategory,
        public finalSchema: SchemaCategory,
        public layoutType: LayoutType,
        public candidates: Candidates,
    ) {}

    static fromServer(input: InferenceJobDataFromServer, info: SchemaCategoryInfo): InferenceJobData {
        console.log('edits in InferenceJobData', input.edits);
        return new InferenceJobData(
            input.edits.map(createInferenceEditFromServer),
            SchemaCategory.fromServerWithInfo(info, input.inferenceSchema, input.inferenceMetadata),
            SchemaCategory.fromServerWithInfo(info, input.finalSchema, input.finalMetadata),
            input.layoutType,
            Candidates.fromServer(input.candidates),
        );
    }
}
