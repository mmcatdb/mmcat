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
        readonly id: Id,
        readonly jobId: Id,
        readonly datasourceId: Id,
        readonly label: string,
        readonly description: string,
        readonly fileType: DatasourceType,
        readonly createdAt: Date,
        readonly executedAt: Date[],
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
