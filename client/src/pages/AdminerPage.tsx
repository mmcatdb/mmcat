import { useEffect, useReducer, useState, useMemo } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Spinner } from '@nextui-org/react';
import { getStateFromURLParams, getURLParamsFromState } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { reducer } from '@/components/adminer/reducer';
import { api } from '@/api';
import { type Datasource, DatasourceType } from '@/types/datasource';

export function AdminerPage() {
    const { datasources: allDatasources } = useLoaderData() as AdminerLoaderData;
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(reducer, searchParams, getStateFromURLParams);
    const [ datasource, setDatasource ] = useState<Datasource>();

    useEffect(() => {
        if (searchParams.get('reload') === 'true')
            dispatch({ type:'initialize' });
    }, [ searchParams ]);

    useEffect(() => {
        const params = getURLParamsFromState(state);

        if (params !== searchParams)
            setSearchParams(params);
    }, [ state ]);

    useMemo(() => {
        setDatasource(allDatasources?.find(source => source.id === state.datasourceId));
    }, [ state.datasourceId, allDatasources ]);

    return (
        <div className='pt-4'>
            <h1 className='text-xl font-bold mb-2'>Adminer for Multi-Model Data</h1>
            <p className='text-muted-foreground mb-2'>Adminer for MM-cat is a tool designed to extend the functionality of MM-cat by enabling users to browse, display, and edit data across multiple database systems, including PostgreSQL, MongoDB, and Neo4j.</p>
            <p className='text-muted-foreground italic text-default-500'>Note: This page is a separate tool and was developed by my colleague. It is not part of my bachelor thesis implementation.</p>
            <div className='mt-5 flex flex-wrap gap-3 items-center'>

                {allDatasources ? (
                    <DatasourceMenu dispatch={dispatch} datasourceId={state.datasourceId} datasources={allDatasources}/>
                ) : (
                    <div className='h-10 flex items-center justify-center'>
                        <Spinner />
                    </div>
                )}

                {datasource &&
                (
                    <>
                        <KindMenu datasourceId={datasource.id} kind={state.kindName} showUnlabeled={datasource.type === DatasourceType.neo4j} dispatch={dispatch}/>

                        {state.kindName !== undefined && (
                            <ViewMenu datasourceType={datasource.type} view={state.view} dispatch={dispatch}/>
                        )}
                    </>
                )}
            </div>

            {datasource && state.kindName && typeof state.kindName === 'string' &&(
                <div className='mt-5'>
                    <DatabaseView state={state} datasources={allDatasources} dispatch={dispatch}/>
                </div>
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
