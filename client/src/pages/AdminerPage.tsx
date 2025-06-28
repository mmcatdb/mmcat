import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, ButtonGroup } from '@heroui/react';
import { usePreferences } from '@/components/PreferencesProvider';
import { AdminerCustomQueryPage } from '@/components/adminer/AdminerCustomQueryPage';
import { AdminerFilterQueryPage } from '@/components/adminer/AdminerFilterQueryPage';
import { getAdminerURLParams, getInitURLParams, getQueryTypeFromURLParams } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { LinkLengthSwitch } from '@/components/adminer/LinkLengthSwitch';
import { api } from '@/api';
import { QueryType } from '@/types/adminer/QueryType';
import type { Datasource } from '@/types/datasource';
import { twJoin } from 'tailwind-merge';

/**
 * Main page of Adminer, data visualization and browsing tool
 */
export function AdminerPage() {
    const { theme } = usePreferences().preferences;
    const { datasources } = useLoaderData() as AdminerLoaderData;
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ selectedQueryType, setSelectedQueryType ] = useState<QueryType>();

    useEffect(() => {
        if (searchParams.get('reload') === 'true')
            window.history.replaceState({}, '', '?' + getInitURLParams(searchParams));

        const datasourceIdParam = searchParams.get('datasourceId');
        if (datasourceIdParam !== datasource?.id)
            setDatasource(datasources?.find(source => source.id === datasourceIdParam));

        setSelectedQueryType(getQueryTypeFromURLParams(searchParams));
    }, [ searchParams ]);

    return (
        <div className='h-full px-8 flex flex-col'>
            <div className={twJoin('flex items-center w-full h-10 border-b px-0',
                theme === 'dark' ? 'border-gray-700' : 'border-gray-300',
            )}>
                <DatasourceMenu setDatasource={setDatasource} datasource={datasource} datasources={datasources}/>

                {datasource && (<>
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
                </>)}
            </div>

            {datasource && (
                selectedQueryType === QueryType.custom ? (
                    <AdminerCustomQueryPage datasource={datasource} datasources={datasources} />
                ) : (
                    <AdminerFilterQueryPage datasource={datasource} datasources={datasources} />
                )
            )}
        </div>
    );
}

AdminerPage.loader = adminerLoader;

type AdminerLoaderData = {
    datasources: Datasource[];
};

async function adminerLoader(): Promise<AdminerLoaderData> {
    const response = await api.datasources.getAllDatasources({});

    if (!response.status)
        throw new Error('Failed to load datasources');

    return {
        datasources: response.data,
    };
}
