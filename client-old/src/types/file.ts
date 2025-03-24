import type { Entity, Id } from './id';

export type FileFromServer = {
    id: Id;
    jobId?: Id;
    datasourceId?: Id;
    categoryId?: Id;
    label: string;
    description?: string;
    jobLabel: string;
    fileType: string; //TODO
    createdAt: string;
    executedAt?: string[];
};

export class File implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly jobId: Id | undefined,
        public readonly datasourceId: Id | undefined,
        public readonly categoryId: Id | undefined,
        public readonly label: string,
        public readonly description: string | undefined,
        public readonly jobLabel: string,
        public readonly fileType: string, //TODO (should this be an enum here?)
        public readonly createdAt: Date,
        public readonly executedAt: Date[] | undefined,
    ) {}

    static fromServer(input: FileFromServer): File {
        return new File(
            input.id,
            input.jobId,
            input.datasourceId,
            input.categoryId,
            input.label,
            input.description,
            input.jobLabel,
            input.fileType,
            new Date(input.createdAt),
            input.executedAt ? input.executedAt.map(dateString => new Date(dateString)) : undefined,
        );
    }
}