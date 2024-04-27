import { Datasource, type DatasourceFromServer } from './datasource';
import type { Id, Entity } from './id';
import { Mapping, type MappingFromServer } from './mapping';

export type LogicalModelInit = {
    datasourceId: Id;
    categoryId: Id;
    label: string;
};

export type LogicalModelInfoFromServer = {
    id: Id;
    label: string;
    datasource: DatasourceFromServer;
};

export class LogicalModelInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly datasource: Datasource,
    ) {}

    static fromServer(input: LogicalModelInfoFromServer): LogicalModelInfo {
        return new LogicalModelInfo(
            input.id,
            input.label,
            Datasource.fromServer(input.datasource),
        );
    }
}

export type LogicalModelFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
    datasource: DatasourceFromServer;
    mappings: MappingFromServer[];
};

export class LogicalModel implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly datasource: Datasource,
        public readonly mappings: Mapping[],
    ) {}

    static fromServer(input: LogicalModelFromServer): LogicalModel {

        return new LogicalModel(
            input.id,
            input.label,
            Datasource.fromServer(input.datasource),
            input.mappings.map(Mapping.fromServer),
        );
    }
}
