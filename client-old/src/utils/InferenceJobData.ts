import type { SchemaCategoryFromServer } from '../types/schema';
import type { Mapping } from '../types/mapping';
import type { AbstractInferenceEdit } from '../types/inferenceEdit/inferenceEdit';

class InferenceData {
    public readonly schemaCategory: SchemaCategoryFromServer;
    public readonly mapping: Mapping;

    constructor(schemaCategory: SchemaCategoryFromServer, mapping: Mapping) {
        this.schemaCategory = schemaCategory;
        this.mapping = mapping;
    }
}

export class InferenceJobData {
    public readonly inference: InferenceData;
    public manual: AbstractInferenceEdit[];
    public finalSchema: SchemaCategoryFromServer;

    constructor(inference: InferenceData) {
        this.inference = inference;
        this.manual = [];
        this.finalSchema = inference.schemaCategory;
    }
}

export function isInferenceJobData(data: any): data is InferenceJobData {
    return (
        data &&
        typeof data === 'object' &&
        'inference' in data &&
        'manual' in data &&
        typeof data.inference === 'object' &&
        'schemaCategory' in data.inference &&
        'mapping' in data.inference
    );
}
/*
export function convertToInferenceJobData(data: any): InferenceJobData | null {
    const parsedData = JSON.parse(data);
    if (isInferenceJobData(parsedData)) {
        const inference = new InferenceData(parsedData.inference.schemaCategory, parsedData.inference.mapping);
        const inferenceJobData = new InferenceJobData(inference);
        inferenceJobData.manual = parsedData.manual;
        inferenceJobData.finalSchema = parsedData.finalSchema;
        return inferenceJobData;
    }
    return null;
}*/