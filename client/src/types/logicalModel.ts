import { DatasourceInfo, DatasourceWithConfiguration, type DatasourceInfoFromServer, type DatasourceWithConfigurationFromServer } from './datasource';
import type { Id, Entity } from './id';
import { Mapping, type MappingFromServer } from './mapping';

export type LogicalModelInit = {
    datasourceId: Id;
    categoryId: Id;
    label: string;
};

export class LogicalModelInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly datasource: DatasourceInfo,
    ) {}

    static fromServer(input: LogicalModelInfoFromServer): LogicalModelInfo {
        return new LogicalModelInfo(
            input.id,
            input.label,
            DatasourceInfo.fromServer(input.datasource),
        );
    }
}

export type LogicalModelInfoFromServer = {
    id: Id;
    label: string;
    datasource: DatasourceInfoFromServer;
};

export class LogicalModel implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly categoryId: Id,
        public readonly datasource: DatasourceWithConfiguration,
        public readonly mappings: Mapping[],
    ) {}

    static fromServer(input: LogicalModelFromServer): LogicalModel {

        return new LogicalModel(
            input.id,
            input.label,
            input.categoryId,
            DatasourceWithConfiguration.fromServer(input.datasource),
            input.mappings.map(Mapping.fromServer),
        );
    }
}

export type LogicalModelFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
    datasource: DatasourceWithConfigurationFromServer;
    mappings: MappingFromServer[];
};
