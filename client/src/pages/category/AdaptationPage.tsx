import { useLoaderData, type Params } from 'react-router-dom';
import { api } from '@/api';
import { PageLayout } from '@/components/RootLayout';
import { Datasource, DatasourceType } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Card, CardBody } from '@heroui/react';
import { DatasourceBadge } from '@/components/datasources/DatasourceBadge';
import { adaptationResultToGraph, type AdaptationResult, type ResultDatasource, type ResultKind } from '@/components/adaptation/kindGraph';
import { useMemo } from 'react';
import { KindGraphDisplay } from '@/components/adaptation/KindGraphDisplay';
import { type Position } from '@/components/graph/graphUtils';
import { type Id } from '@/types/id';

export function AdaptationPage() {
    const { datasources, mappings } = useLoaderData() as AdaptationPageData;

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>Adaptation</h1>

            <div className='flex gap-4'>
                <div>Datasources: {datasources.length}</div>
                <div>Kinds: {mappings.length}</div>
            </div>

            <h2 className='text-lg font-semibold'>Table view</h2>

            <div className='flex justify-center gap-4'>
                <div className='py-3 flex flex-col gap-1'>
                    <div className='h-6' />

                    {mockResults[0].kinds.map(kind => (
                        <div key={kind.id} className='leading-6 font-medium'>
                            {kind.label}
                        </div>
                    ))}
                </div>

                {mockResults.map((result, index) => (
                    <AdaptationResultColumn key={index} result={result} />
                ))}
            </div>

            <h2 className='text-lg font-semibold'>Graph view</h2>

            <div className='flex flex-col gap-4'>
                {mockResults.map((result, index) => (
                    <AdaptationResultGraph key={index} result={result} />
                ))}
            </div>
        </PageLayout>
    );
}

type AdaptationPageData = {
    datasources: Datasource[];
    mappings: Mapping[];
};

AdaptationPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId' | 'queryId'> }): Promise<AdaptationPageData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const [ datasourcesResponse, mappingsResponse ] = await Promise.all([
        api.datasources.getAllDatasources({}, { categoryId }),
        api.mappings.getAllMappingsInCategory({}, { categoryId }),
    ]);
    if (!datasourcesResponse.status || !mappingsResponse.status)
        throw new Error('Failed to load query info');

    return {
        datasources: datasourcesResponse.data.map(Datasource.fromResponse),
        mappings: mappingsResponse.data.map(Mapping.fromResponse),
    };
};

function AdaptationResultColumn({ result }: { result: AdaptationResult }) {
    return (
        <Card>
            <CardBody className='flex flex-col items-start gap-1'>
                <div className='h-6 self-end font-semibold'>
                    {result.price}
                </div>

                {result.kinds.map(kind => (
                    <div key={kind.id} className='w-full flex items-center gap-2'>
                        <DatasourceBadge type={kind.datasource.type} />

                        <div className='grow tabular-nums text-right'>
                            {Math.round(100 * kind.improvement)} %
                        </div>
                    </div>
                ))}
            </CardBody>
        </Card>
    );
}

function AdaptationResultGraph({ result }: { result: AdaptationResult }) {
    const graph = useMemo(() => adaptationResultToGraph(result), [ result ]);

    return (
        <Card className='relative'>
            <div className='absolute left-1 top-1 px-2 py-1 rounded-lg font-semibold z-50 bg-black'>
                {result.price}
            </div>
            <KindGraphDisplay graph={graph} className='h-[300px]' />
        </Card>
    );
}

const resultDatasources: ResultDatasource[] = [
    { id: 'postgresql', label: 'PostgreSQL', type: DatasourceType.postgresql },
    { id: 'mongodb', label: 'MongoDB', type: DatasourceType.mongodb },
    { id: 'neo4j', label: 'Neo4j', type: DatasourceType.neo4j },
];

const resultKinds: Omit<ResultKind, 'datasource' | 'improvement'>[] = [
    { id: 'customer', label: 'Customer', toRelationships: [] },
    { id: 'order', label: 'Order', toRelationships: [ 'customer' ] },
    { id: 'product', label: 'Product', toRelationships: [] },
    { id: 'orderItem', label: 'Product', toRelationships: [ 'order', 'product' ] },
];

const positions = new Map<Id, Position>();

resultKinds.forEach(kind => positions.set(kind.id, getRandomPosition()));

function getRandomPosition(): Position {
    return {
        x: Math.random() * 800,
        y: Math.random() * 600,
    };
}

function createResult(price: number, kindsForDatasources: Record<string, string[]>): AdaptationResult {
    const kinds: ResultKind[] = [];

    for (const datasourceId in kindsForDatasources) {
        const datasource = resultDatasources.find(d => d.id === datasourceId)!;

        const kindIds = kindsForDatasources[datasourceId];
        for (const kindId of kindIds) {
            const kindTemplate = resultKinds.find(k => k.id === kindId)!;
            kinds.push({
                ...kindTemplate,
                datasource,
                improvement: Math.random(),
            });
        }
    }

    return {
        datasources: resultDatasources,
        kinds: kinds.toSorted((a, b) => a.label.localeCompare(b.label)),
        price,
        positions,
    };
}

const mockResults: AdaptationResult[] = [
    createResult(42.37, {
        postgresql: [ 'customer', 'order' ],
        mongodb: [ 'product' ],
        neo4j: [ 'orderItem' ],
    }),
    createResult(55.91, {
        mongodb: [ 'product', 'customer', 'order' ],
        neo4j: [ 'orderItem' ],
    }),
];
