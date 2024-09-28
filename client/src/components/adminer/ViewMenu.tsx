import { useState, useEffect } from 'react';
import { Button } from '@nextui-org/react';
import { DatasourceType } from '@/types/datasource';
import { View } from '@/types/adminer/View';

type ViewMenuProps = Readonly<{
    datasourceType: DatasourceType;
    view: View;
    setView: (view: View) => void;
}>;

export function ViewMenu({ datasourceType, view, setView }: ViewMenuProps) {
    const [ tableButtonVisible,  setTableButtonVisible ] = useState<boolean>(false);
    const [ documentButtonVisible,  setDocumentButtonVisible ] = useState<boolean>(false);

    useEffect(() => {
        switch(datasourceType) {
        case DatasourceType.postgresql:
            setTableButtonVisible(true);
            setDocumentButtonVisible(false);

            if (view !== View.table)
                setView(View.table);

            break;
        case DatasourceType.mongodb:
            setTableButtonVisible(false);
            setDocumentButtonVisible(true);

            if (view !== View.document)
                setView(View.document);

            break;
        case DatasourceType.neo4j:
            setTableButtonVisible(true);
            setDocumentButtonVisible(true);
            break;
        default:
            throw new Error('Invalid datasource type');
        }
    }, [ datasourceType, tableButtonVisible, documentButtonVisible, view, setView ]);

    return (
        <div className='flex gap-3 items-center'>
            {tableButtonVisible && (
                <Button
                    key={'table'}
                    onPress={() => setView(View.table)}
                    color={view === View.table ? 'primary' : 'default'}
                    className='min-w-[50px] max-w-[200px]'
                >
                    Table
                </Button>
            )}

            {documentButtonVisible && (
                <Button
                    key={'document'}
                    onPress={() => setView(View.document)}
                    color={view === View.document ? 'primary' : 'default'}
                    className='min-w-[50px] max-w-[200px]'
                >
                        JSON
                </Button>
            )}
        </div>
    );
}
