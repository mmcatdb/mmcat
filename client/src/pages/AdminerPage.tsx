import clsx from 'clsx';
import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, ButtonGroup } from '@nextui-org/react';
import { ErrorPage } from '@/pages/errorPages';
import { usePreferences } from '@/components/PreferencesProvider';
import { AdminerCustomQueryPage } from '@/components/adminer/AdminerCustomQueryPage';
import { AdminerFilterQueryPage } from '@/components/adminer/AdminerFilterQueryPage';
import { getAdminerURLParams, getInitURLParams, getQueryTypeFromURLParams } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { LinkLengthSwitch } from '@/components/adminer/LinkLengthSwitch';
import { api } from '@/api';
import { QueryType } from '@/types/adminer/QueryType';
import type { Datasource } from '@/types/datasource';

type FetchDatasourcesResult = {
    allDatasources: Datasource[];
    error: false;
} | {
    allDatasources: undefined;
    error: true;
};

export async function adminerLoader(): Promise<FetchDatasourcesResult> {
    const response = await api.datasources.getAllDatasources({});

    if (!response.status)
        return { allDatasources: undefined, error: true };

    return { allDatasources: response.data, error: false };
}

export function AdminerPage() {
    const { theme } = usePreferences().preferences;
    const { allDatasources, error } = useLoaderData() as FetchDatasourcesResult;
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ selectedQueryType, setSelectedQueryType ] = useState<QueryType>();

    useEffect(() => {
        if (searchParams.get('reload') === 'true')
            setSearchParams(prevParams => getInitURLParams(prevParams));

        const datasourceIdParam = searchParams.get('datasourceId');
        if (datasourceIdParam !== datasource?.id)
            setDatasource(allDatasources?.find(source => source.id === datasourceIdParam));

        setSelectedQueryType(getQueryTypeFromURLParams(searchParams));
    }, [ searchParams ]);

    if (error)
        return <ErrorPage />;

    return (
        <>
            <div className={clsx(
                'flex items-center w-full h-10 border-b px-0',
                theme === 'dark' ? 'border-gray-700' : 'border-gray-300',
            )}>
                <DatasourceMenu setDatasource={setDatasource} datasource={datasource} datasources={allDatasources}/>

                {datasource && (
                    <>
                        <ButtonGroup
                            className='mx-2'
                        >
                            {Object.values(QueryType).map(queryType => (
                                <Button
                                    size='sm'
                                    variant={queryType === selectedQueryType ? 'solid' : 'ghost'}
                                    key={queryType}
                                    onPress={() => setSearchParams(prevParams => getAdminerURLParams(prevParams, queryType))}
                                >
                                    {queryType}
                                </Button>
                            ),
                            )}
                        </ButtonGroup>

                        <LinkLengthSwitch/>
                    </>
                )}
            </div>

            {datasource && (
                selectedQueryType === QueryType.custom ? (
                    <AdminerCustomQueryPage datasource={datasource} datasources={allDatasources} />
                ) : (
                    <AdminerFilterQueryPage datasource={datasource} datasources={allDatasources} />
                )
            )}
        </>
    );
}
