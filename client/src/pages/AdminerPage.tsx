import clsx from 'clsx';
import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, ButtonGroup, Spinner } from '@nextui-org/react';
import { usePreferences } from '@/components/PreferencesProvider';
import { AdminerCustomQueryPage } from '@/components/adminer/AdminerCustomQueryPage';
import { AdminerFilterQueryPage } from '@/components/adminer/AdminerFilterQueryPage';
import { getAdminerURLParams, getInitURLParams, getQueryTypeFromURLParams } from '@/components/adminer/URLParamsState';
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

    useEffect(() => {
        if (searchParams.get('reload') === 'true')
            setSearchParams(prevParams => getInitURLParams(prevParams));

        const datasourceIdParam = searchParams.get('datasourceId');
        if (datasourceIdParam !== datasource?.id)
            setDatasource(allDatasources?.find(source => source.id === datasourceIdParam));
    }, [ searchParams ]);

    return (
        <>
            <div className={clsx(
                'flex items-center w-full h-10 border-b px-0',
                theme === 'dark' ? 'border-gray-700' : 'border-gray-300',
            )}>
                {allDatasources ? (
                    // FIXME Pokud v případě, že datasources nejsou k dispozici, nedává smysl zobrazit nic kromě spinneru, můžete to řešit už na řádku 38 a rovnou vrátit div se spinnerem.
                    <>
                        <DatasourceMenu setDatasource={setDatasource} datasourceId={datasource?.id} datasources={allDatasources}/>

                        {datasource && (
                            <>
                                <ButtonGroup
                                    className='mx-2'
                                >
                                    {Object.values(QueryType).map(queryType => (
                                        <Button
                                            size='sm'
                                            // FIXME Toto je drobnost, ale aria-label se používá jako dodatečná informace pro tlačítko. Měl by tedy popisovat, co tlačítko dělá, tj. by neměl být stejný pro obě tlačítka.
                                            // Nicméně pokud máte v tlačítku text (což teď máte), tak je to redundantní a je lepší ho vynechat. Hodí se spíš pro tlačítka, která mají jen ikonu či obrázek.
                                            // Opravil bych to všude v kódu.
                                            aria-label='Query type'
                                            // FIXME Jaký formulář totot tlačítko submituje? Taky bych to sem nedával.
                                            // Opravil bych to všude v kódu.
                                            type='submit'
                                            // FIXME Aktuální query type si spočítejte někde najednou a pak to použijte tady i o pár řádků níž.
                                            variant={queryType === getQueryTypeFromURLParams(searchParams) ? 'solid' : 'ghost'}
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
                    </>
                ) : (
                    <span className='h-8 flex items-center justify-center'>
                        <Spinner />
                    </span>
                )}
            </div>

            {datasource && (
                getQueryTypeFromURLParams(searchParams) === QueryType.custom ? (
                    // FIXME Pointou contextu v reactu je, že ho můžete používat kdekoli ve stromu komponent, a nemusíte ho předávat jako props. Tedy příslušné komponenty by si měly téma zjistit samy.
                    <AdminerCustomQueryPage datasource={datasource} datasources={allDatasources} theme={theme}/>
                ) : (
                    <AdminerFilterQueryPage datasource={datasource} datasources={allDatasources} theme={theme}/>
                )
            )}
        </>
    );
}
