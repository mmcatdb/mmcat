import type { DatasourceType } from './Datasource';
import type { Entity, Id } from './id';

export type FileResponse = {
    id: Id;
    jobId: Id;
    datasourceId: Id;
    label: string;
    description: string;
    fileType: DatasourceType;
    createdAt: string;
    executedAt: string[];
};

export class File implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly jobId: Id,
        public readonly datasourceId: Id,
        public readonly label: string,
        public readonly description: string,
        public readonly fileType: DatasourceType,
        public readonly createdAt: Date,
        public readonly executedAt: Date[],
    ) {}

    static fromResponse(input: FileResponse): File {
        return new File(
            input.id,
            input.jobId,
            input.datasourceId,
            input.label,
            input.description,
            input.fileType,
            new Date(input.createdAt),
            input.executedAt.map(dateString => new Date(dateString)),
        );
    }
}

export type FileEdit = {
    label: string;
    description: string;
};
