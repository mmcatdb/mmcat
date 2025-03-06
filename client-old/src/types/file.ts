import type { Entity, Id } from './id';

export type FileFromServer = {
    id: Id;
    jobId?: Id;
    datasourceId?: Id;
    categoryId?: Id;
    label: string;
    fileType: string; //TODO
    createdAt: string;
};

export class File implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly jobId: Id | undefined,
        public readonly datasourceId: Id | undefined,
        public readonly categoryId: Id | undefined,
        public readonly label: string,
        public readonly fileType: string, //TODO (should this be an enum here?)
        public readonly createdAt: Date,
    ) {}

    static fromServer(input: FileFromServer): File {
        return new File(
            input.id,
            input.jobId,
            input.datasourceId,
            input.categoryId,
            input.label,
            input.fileType,
            new Date(input.createdAt),
        );
    }
}