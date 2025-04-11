import clsx from 'clsx';
import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, ButtonGroup, Spinner } from '@nextui-org/react';
import { AdminerCustomQueryPage } from '@/pages/AdminerCustomQueryPage';
import { AdminerFilterQueryPage } from '@/pages/AdminerFilterQueryPage';
import { usePreferences } from '@/components/PreferencesProvider';
import { getAdminerURLParams, getInitURLParams } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { LinkLengthSwitch } from '@/components/adminer/LinkLengthSwitch';
import { api } from '@/api';
import { QueryType } from '@/types/adminer/QueryType';
import type { Datasource } from '@/types/datasource';

export async function adminerLoader(): Promise<Datasource[]> {
    const response = await api.datasources.getAllDatasources({});

    if (!response.status)
        throw new Error('Failed to load datasources');

    return response.data;
}

export function AdminerPage() {
    const { theme } = usePreferences().preferences;
    const allDatasources = useLoaderData() as Datasource[];
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ queryType, setQueryType ] = useState<QueryType>();

    useEffect(() => {
        const queryTypeParam = searchParams.get('queryType');
        setQueryType(Object.values(QueryType).includes(queryTypeParam as QueryType)
            ? (queryTypeParam as QueryType)
            : QueryType.filter);

        if (searchParams.get('reload') === 'true')
            setSearchParams(prevParams => getInitURLParams(prevParams));

        const datasourceIdParam = searchParams.get('datasourceId');
        if (datasourceIdParam !== datasource?.id)
            setDatasource(allDatasources?.find(source => source.id === datasourceIdParam));
    }, [ searchParams ]);

    useEffect(() => {
        const queryTypeParam = searchParams.get('queryType');
        if (queryType !== queryTypeParam)
            setSearchParams(prevParams => getAdminerURLParams(prevParams, queryType));
    }, [ queryType ]);

    return (
        <div>
            <div className={clsx(
                'flex items-center z-20 w-full h-12 border-b px-0',
                theme === 'dark' ? 'border-gray-700' : 'border-gray-300',
            )}>
                {allDatasources ? (
                    <>
                        <DatasourceMenu setDatasource={setDatasource} datasourceId={searchParams.get('datasourceId') ?? datasource?.id} datasources={allDatasources}/>

                        <ButtonGroup
                            className='mx-2'
                        >
                            {Object.values(QueryType).map(qt => (
                                <Button
                                    size='sm'
                                    aria-label='Query type'
                                    type='submit'
                                    variant={qt === queryType ? 'solid' : 'ghost'}
                                    key={qt}
                                    onPress={() => setQueryType(qt)}
                                >
                                    {qt}
                                </Button>
                            ),
                            )}
                        </ButtonGroup>

                        <LinkLengthSwitch/>
                    </>

                ) : (
                    <div className='h-8 flex items-center justify-center'>
                        <Spinner />
                    </div>
                )}
            </div>

            {datasource && (
                queryType === QueryType.custom ? (
                    <AdminerCustomQueryPage datasource={datasource} datasources={allDatasources}/>
                ) : (
                    <AdminerFilterQueryPage datasource={datasource} datasources={allDatasources}/>
                )
            )}
        </div>
    );
}
