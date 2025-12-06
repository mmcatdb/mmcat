import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, ButtonGroup, Switch } from '@heroui/react';
import { usePreferences } from '@/components/context/PreferencesProvider';
import { AdminerCustomQueryPage } from '@/components/adminer/AdminerCustomQueryPage';
import { AdminerFilterQueryPage } from '@/components/adminer/AdminerFilterQueryPage';
import { getAdminerURLParams, getInitURLParams, getQueryTypeFromURLParams, QueryType } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { api } from '@/api';
import { Datasource } from '@/types/Datasource';
import { PageLayout } from '@/components/RootLayout';

/**
 * Main page of Adminer, data visualization and browsing tool
 */
export function AdminerPage() {
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
        <PageLayout isFullscreen className='flex flex-col'>
            <div className='min-w-4xl p-1 grid grid-cols-3 gap-4 border-b border-default-200'>
                <DatasourceMenu setDatasource={setDatasource} datasource={datasource} datasources={datasources} />

                {datasource && (<>
                    <div className='col-span-2 flex items-center gap-2 justify-between'>
                        <ButtonGroup>
                            {Object.values(QueryType).map(queryType => (
                                <Button
                                    size='sm'
                                    variant={queryType === selectedQueryType ? 'solid' : 'ghost'}
                                    key={queryType}
                                    onPress={() => setSearchParams(prevParams => getAdminerURLParams(prevParams, queryType))}
                                >
                                    {queryType}
                                </Button>
                            ))}
                        </ButtonGroup>

                        <LinkLengthSwitch />
                    </div>
                </>)}
            </div>

            {datasource && (
                selectedQueryType === QueryType.custom ? (
                    <AdminerCustomQueryPage datasource={datasource} datasources={datasources} />
                ) : (
                    <AdminerFilterQueryPage datasource={datasource} datasources={datasources} />
                )
            )}
        </PageLayout>
    );
}

type AdminerLoaderData = {
    datasources: Datasource[];
};

AdminerPage.loader = async (): Promise<AdminerLoaderData> => {
    const response = await api.datasources.getAllDatasources({});

    if (!response.status)
        throw new Error('Failed to load datasources');

    return {
        datasources: response.data.map(Datasource.fromResponse),
    };
};

/**
 * Switch for setting the length of names of datasources, kinds and properties used in links
 */
function LinkLengthSwitch() {
    const { preferences, setPreferences } = usePreferences();
    const { adminerShortLinks } = preferences;

    return (
        <Switch
            isSelected={adminerShortLinks}
            onValueChange={value => setPreferences({ ...preferences, adminerShortLinks: value })}
            size='sm'
        >
            Short links
        </Switch>
    );
}
