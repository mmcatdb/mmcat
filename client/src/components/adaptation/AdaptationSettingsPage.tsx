import { PageLayout } from '@/components/RootLayout';
import { type Datasource } from '@/types/Datasource';
import { type Category } from '@/types/schema';
import { type Dispatch, useMemo } from 'react';
import { type AdaptationMorphism, type Adaptation, adaptationJobFromResponse, type AdaptationJob } from './adaptation';
import { Button, Card, Checkbox, NumberInput, Select, SelectItem } from '@heroui/react';
import { InfoBanner, InfoTooltip } from '../common/components';
import { useBannerState } from '@/types/utils/useBannerState';
import { KindGraphDisplay } from './KindGraphDisplay';
import { type AdaptationSettingsDispatch, type AdaptationSettingsState, useAdaptationSettings } from './useAdaptationSettings';
import { QueriesTable } from '../querying/QueriesTable';
import { type Query } from '@/types/query';
import { dataSizeQuantity, prettyPrintInt } from '@/types/utils/common';
import { api } from '@/api';

type AdaptationSettingsPageProps = {
    category: Category;
    datasources: Datasource[];
    queries: Query[];
    updateQuery: Dispatch<Query>;
    adaptation: Adaptation;
    onNext: (job: AdaptationJob) => void;
};

export function AdaptationSettingsPage({ category, datasources, queries, updateQuery, adaptation, onNext }: AdaptationSettingsPageProps) {
    const banner = useBannerState('adaptation-settings-page');

    const { state, dispatch } = useAdaptationSettings(category, adaptation);

    // NICE_TO_HAVE
    // function saveAdaptation() {

    // }

    async function startAdaptation() {
        const response = await api.adaptations.startAdaptation({ adaptationId: adaptation.id });
        if (!response.status) {
            // TODO handle error
            console.error(response.error);
            return;
        }

        onNext(adaptationJobFromResponse(response.data, category, datasources, queries));
    }

    return (
        <PageLayout>
            <div className='flex items-center gap-2 mb-4'>
                <h1 className='text-xl font-bold text-default-800'>Adaptation</h1>

                <InfoTooltip {...banner} />
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <AdaptationSettingsInfoInner />
            </InfoBanner>

            {/* <h2 className='mb-2 text-lg font-semibold'>Parameters</h2>

            <div className='mb-4 grid grid-cols-3 gap-4'>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    label='Exploration weight'
                    value={state.form.explorationWeight}
                    onValueChange={value => dispatch({ type: 'form', field: 'explorationWeight', value })}
                />

                <Select
                    className='col-span-2'
                    selectionMode='multiple'
                    label='Enabled datasources'
                    selectedKeys={state.form.datasourceIds}
                    placeholder='Select a datasource'
                    onSelectionChange={keys => dispatch({ type: 'form', field: 'datasourceIds', value: keys as Set<string> })}
                    items={datasources}
                >
                    {datasource => (
                        <SelectItem key={datasource.id}>{datasource.label}</SelectItem>
                    )}
                </Select>
            </div> */}

            <h2 className='mb-2 text-lg font-semibold'>Kinds & Relationships</h2>

            <div className='mb-2 grid grid-cols-4 gap-4'>
                <div className='col-span-3'>
                    <Card>
                        <KindGraphDisplay graph={state.graph} selection={state.selection} dispatch={dispatch} className='h-[600px]' />
                    </Card>
                </div>

                <Card className='p-4'>
                    {state.selection.firstNodeId ? (
                        <NodeEditor state={state} dispatch={dispatch} />
                    ) : state.selection.firstEdgeId ? (
                        <EdgeEditor state={state} dispatch={dispatch} />
                    ) : (
                        <div>Select a kind or relationship to edit its details.</div>
                    )}
                </Card>
            </div>

            <div className='mb-4 flex items-center gap-8'>
                <AggregatedMorphismsOperationsForm state={state} dispatch={dispatch} />
            </div>

            <h2 className='mb-2 text-lg font-semibold'>Queries</h2>

            <div className='mb-4'>
                <QueriesTable queries={queries} itemsPerPage={5} onUpdate={updateQuery} />
            </div>

            <div className='flex justify-end gap-2'>
                {/* NICE_TO_HAVE
                <Button color='success' onPress={saveAdaptation}>
                    Save & Continue editing
                </Button> */}

                <Button color='success' onPress={startAdaptation}>
                    Start Adaptation
                </Button>
            </div>
        </PageLayout>
    );
}

type StateDispatchProps = {
    state: AdaptationSettingsState;
    dispatch: AdaptationSettingsDispatch;
};

function AggregatedMorphismsOperationsForm({ state, dispatch }: StateDispatchProps) {
    const morphisms = state.form.morphisms;

    const { isReferenceAllowed, isEmbeddingAllowed } = useMemo(() => {
        let references = 0;
        let embeddings = 0;

        morphisms.values().forEach(m => {
            references += m.isReferenceAllowed ? 1 : 0;
            embeddings += m.isEmbeddingAllowed ? 1 : 0;
        });

        return {
            isReferenceAllowed: references === morphisms.size ? true : references === 0 ? false : undefined,
            isEmbeddingAllowed: embeddings === morphisms.size ? true : embeddings === 0 ? false : undefined,
        };
    }, [ morphisms ]);

    function setMorphisms(edit: Partial<Omit<AdaptationMorphism, 'signature'>>) {
        dispatch({ type: 'form', field: 'morphisms', edit });
    }

    return (<>
        <h3 className='text-lg font-semibold'>Allowed edge operations</h3>

        <Checkbox isSelected={!!isReferenceAllowed} isIndeterminate={isReferenceAllowed === undefined} onValueChange={value => setMorphisms({ isReferenceAllowed: value })}>
            Reference
        </Checkbox>

        <Checkbox isSelected={!!isEmbeddingAllowed} isIndeterminate={isEmbeddingAllowed === undefined} onValueChange={value => setMorphisms({ isEmbeddingAllowed: value })}>
            Embedding
        </Checkbox>
    </>);
}

function NodeEditor({ state, dispatch }: StateDispatchProps) {
    const { graph, selection } = state;

    const selectedNode = graph.nodes.get(selection.firstNodeId!)!;
    const objex = state.adaptation.settings.objexes.get(selectedNode.objex.key);

    // const items = useMemo(() => {
    //     return datasources
    //         .filter(item =>
    //             [ DatasourceType.postgresql, DatasourceType.mongodb, DatasourceType.neo4j ].includes(item.type),
    //         );
    // }, [ datasources ]);

    // const datasource = selectedNode.datasource;

    // function setDatasource(datasource: Datasource) {
    //     // TODO Update adaptation settings and graph
    // }

    const mapping = objex?.mappings.length ? objex.mappings[0] : undefined;

    return (<>
        <h3 className='mb-2 text-lg font-semibold'>Inspect Kind</h3>

        <div className='text-sm font-semibold text-foreground-400'>Label</div>
        {selectedNode.objex.metadata.label}

        {mapping && (mapping.dataSizeInBytes || mapping.recordCount) && (<>
            <div className='mt-2 text-sm font-semibold text-foreground-400'>Data size</div>
            {mapping.dataSizeInBytes && (
                <div>{dataSizeQuantity.prettyPrint(mapping.dataSizeInBytes)}</div>
            )}
            {mapping.recordCount && (
                <div>{prettyPrintInt(mapping.recordCount)} records</div>
            )}
        </>)}

        <div className='mt-2 text-sm font-semibold text-foreground-400'>Properties</div>
        <ul className='pl-5 list-disc'>
            {[ ...selectedNode.properties.values() ].map(value => (
                <li key={value.key.toString()} className=''>
                    {value.metadata.label}
                </li>
            ))}
        </ul>
    </>);
}

function EdgeEditor({ state, dispatch }: StateDispatchProps) {
    const { selection } = state;

    const edgeId = selection.firstEdgeId!;
    const edge = state.form.morphisms.get(edgeId);

    function setMorphism(edit: Partial<Omit<AdaptationMorphism, 'signature'>>) {
        dispatch({ type: 'form', field: 'morphism', edgeId, edit });
    }

    return (<>
        <h3 className='mb-2 text-lg font-semibold'>Inspect Relationship</h3>

        <h4 className='mb-1 text-sm font-semibold text-foreground-400'>Allowed operations</h4>
        <Checkbox isSelected={edge?.isReferenceAllowed ?? false} onValueChange={value => setMorphism({ isReferenceAllowed: value })} isDisabled={!edge}>
            Reference
        </Checkbox>

        <Checkbox isSelected={edge?.isEmbeddingAllowed ?? false} onValueChange={value => setMorphism({ isEmbeddingAllowed: value })} isDisabled={!edge}>
            Embedding
        </Checkbox>
    </>);
}

function AdaptationSettingsInfoInner() {
    return (<>
        <h2>Adaptation Settings</h2>

        <p>
            Configure how the advisor explores mapping alternatives. Choose algorithm parameters, select which datasources to consider, and tune per-query weights. The visualization lets you inspect entities and edge options.
        </p>

        <ul>
            <li>
                <span className='font-bold'>Parameters:</span> E.g., exploration weight for MCTS — affects search trade-off between exploring and exploiting.
            </li>
            <li>
                <span className='font-bold'>Graph editing:</span> Click a node to set its default datasource; click an edge to allow reference/embedding.
            </li>
            <li>
                <span className='font-bold'>Query weights:</span> By default, they are equal to execution counts, but you can override them here.
            </li>
        </ul>

        <p>
            Save anytime. When ready, use <span className='font-bold'>Start</span> to launch a job that will run the optimization on the server.
        </p>
    </>);
}
