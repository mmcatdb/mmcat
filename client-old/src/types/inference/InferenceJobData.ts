import { JobDataType } from '../job';
import { SchemaCategory, type SchemaCategoryFromServer } from '../schema';
import type { InferenceEdit } from './inferenceEdit';

type InferenceDataFromServer = {
    schemaCategory: SchemaCategoryFromServer;
};

export type InferenceJobDataFromServer = {
    type: JobDataType.Inference;
    inference: InferenceDataFromServer;
    manual: InferenceEdit[];
    finalSchema: SchemaCategoryFromServer;
};

export class InferenceJobData {
    constructor(
        public manual: InferenceEdit[],
        public finalSchema: SchemaCategory,
    ) {}

    static fromServer(input: InferenceJobDataFromServer): InferenceJobData {
        return new InferenceJobData(
            input.manual,
            SchemaCategory.fromServer(input.finalSchema, []),
        );
    }
}
