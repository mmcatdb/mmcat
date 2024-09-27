import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseList } from '@/components/adminer/DatabaseList';
import { DatasourceType } from '@/types/datasource';

type DatabaseViewProps = Readonly<{
    apiUrl: string;
    datasourceType: DatasourceType;
}>;

export function DatabaseView({ apiUrl, datasourceType }: DatabaseViewProps) {
    return (
        datasourceType === DatasourceType.postgresql ? (
            <DatabaseTable apiUrl={apiUrl} />
        ) : (
            <DatabaseList apiUrl={apiUrl} />
        )
    );
}
