import { View } from '@/types/adminer/View';
import { DatasourceType } from '@/types/datasource';

export const AVAILABLE_VIEWS: Record<DatasourceType, View[]> = {
    [DatasourceType.neo4j]: [ View.table, View.document, View.graph ],
    [DatasourceType.mongodb]: [ View.document ],
    [DatasourceType.postgresql]: [ View.table ],
    [DatasourceType.csv]: [ View.table ],
    [DatasourceType.json]: [ View.document ],
    [DatasourceType.jsonld]: [ View.document ],
};

export function getNewView(oldView: View, newType: DatasourceType): View {
    return AVAILABLE_VIEWS[newType].includes(oldView)
        ? oldView
        : AVAILABLE_VIEWS[newType][0];
}
