import type { SchemaCategoryFromServer } from '../types/schema';
import type { Mapping } from '../types/mapping';

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
    public manual: string[];
    public finalSchema: SchemaCategoryFromServer;

    constructor(inference: InferenceData) {
        this.inference = inference;
        this.manual = [];
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