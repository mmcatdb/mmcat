import type { JobDataType } from '../job';
import { SchemaCategory, type SchemaCategoryInfo, type SerializedSchema, type SerializedMetadata } from '../schema';
import type { InferenceEdit } from './inferenceEdit';
import { createInferenceEditFromServer } from './inferenceEdit';

export type InferenceJobDataFromServer = {
    type: JobDataType.Inference;
    edits: InferenceEdit[];
    schema: SerializedSchema;
    metadata: SerializedMetadata;
    // mappings
};

export class InferenceJobData {
    constructor(
        public edits: InferenceEdit[],
        public schema: SchemaCategory,
    ) {}

    static fromServer(input: InferenceJobDataFromServer, info: SchemaCategoryInfo): InferenceJobData {
        return new InferenceJobData(
            //input.edits,
            input.edits.map(createInferenceEditFromServer),
            SchemaCategory.fromServerWithInfo(info, input.schema, input.metadata),
        );
    }
}
