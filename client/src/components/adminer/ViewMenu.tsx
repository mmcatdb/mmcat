import { Button } from '@nextui-org/react';
import { DatasourceType } from '@/types/datasource';
import { View } from '@/types/adminer/View';
import { type AdminerStateAction } from '@/types/adminer/Reducer';

type ViewMenuProps = Readonly<{
    datasourceType: DatasourceType;
    view: View;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function ViewMenu({ datasourceType, view, dispatch }: ViewMenuProps) {
    return (
        <div className='flex gap-3 items-center'>
            {(datasourceType == DatasourceType.postgresql || datasourceType == DatasourceType.neo4j) && (
                <Button
                    key={'table'}
                    onPress={() => dispatch({ type:'view', newView: View.table })}
                    color={view === View.table ? 'primary' : 'default'}
                    className='min-w-[50px] max-w-[200px]'
                >
                    Table
                </Button>
            )}

            {(datasourceType == DatasourceType.mongodb || datasourceType == DatasourceType.neo4j) && (
                <Button
                    key={'document'}
                    onPress={() => dispatch({ type:'view', newView: View.document })}
                    color={view === View.document ? 'primary' : 'default'}
                    className='min-w-[50px] max-w-[200px]'
                >
                        JSON
                </Button>
            )}
        </div>
    );
}
