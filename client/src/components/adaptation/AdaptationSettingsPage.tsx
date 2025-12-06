import { PageLayout } from '@/components/RootLayout';
import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Category } from '@/types/schema';
import { type Dispatch, useMemo } from 'react';
import { type AdaptationMorphism, type Adaptation } from './adaptation';
import { Button, Card, Checkbox, NumberInput, Select, SelectItem } from '@heroui/react';
import { InfoBanner, InfoTooltip } from '../common/components';
import { useBannerState } from '@/types/utils/useBannerState';
import { type Job } from '@/types/job';
import { KindGraphDisplay } from './KindGraphDisplay';
import { type AdaptationSettingsDispatch, type AdaptationSettingsState, useAdaptationSettings } from './useAdaptationSettings';
import { DatasourceBadge } from '../datasource/DatasourceBadge';
import { getEdgeSignature } from '../category/graph/categoryGraph';
import { QueriesTable } from '../querying/QueriesTable';
import { type Query } from '@/types/query';

type AdaptationSettingsPageProps = {
    category: Category;
    datasources: Datasource[];
    queries: Query[];
    updateQuery: Dispatch<Query>;
    adaptation: Adaptation;
    onNext: (job: Job) => void;
    /** @deprecated */
    onNextMock?: () => void;
};

export function AdaptationSettingsPage({ category, datasources, queries, updateQuery, adaptation, onNext, onNextMock }: AdaptationSettingsPageProps) {
    const banner = useBannerState('adaptation-settings-page');

    const { state, dispatch } = useAdaptationSettings(category, adaptation);

    function saveAdaptation() {

    }

    function startAdaptation() {
        onNextMock?.();
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

            <h2 className='mb-2 text-lg font-semibold'>Parameters</h2>

            <div className='mb-4 grid grid-cols-3 gap-4'>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    label='Exploration weight'
                    value={state.form.explorationWeight}
                    onValueChange={value => dispatch({ type: 'form', explorationWeight: value })}
                />
            </div>

            <h2 className='mb-2 text-lg font-semibold'>Kinds & Relationships</h2>

            <div className='mb-4 grid grid-cols-4 gap-4'>
                <Card className='col-span-3'>
                    <KindGraphDisplay graph={state.graph} selection={state.selection} dispatch={dispatch} className='h-[300px]' />
                </Card>

                <Card className='p-4'>
                    {state.selection.firstNodeId ? (
                        <NodeEditor state={state} dispatch={dispatch} datasources={datasources} />
                    ) : state.selection.firstEdgeId ? (
                        <EdgeEditor state={state} dispatch={dispatch} />
                    ) : (
                        <div>Select a kind or relationship to edit its details.</div>
                    )}
                </Card>
            </div>

            <h2 className='mb-2 text-lg font-semibold'>Queries</h2>

            <div className='mb-4'>
                <QueriesTable queries={queries} itemsPerPage={5} onUpdate={updateQuery} />
            </div>

            <div className='flex justify-end gap-2'>
                <Button color='success' onPress={saveAdaptation}>
                    Save & Continue editing
                </Button>

                <Button color='primary' onPress={startAdaptation}>
                    Start Adaptation
                </Button>
            </div>
        </PageLayout>
    );
}

function AdaptationSettingsInfoInner() {
    return (<>
        <h2 className='text-lg font-semibold mb-2'>Adaptation Input</h2>
        <p>
            Configure the settings for adapting your schema category. You can review and adjust the datasources and mappings before proceeding to the adaptation results.
        </p>

        TODO
    </>);
}

type NodeEditorProps = {
    state: AdaptationSettingsState;
    dispatch: AdaptationSettingsDispatch;
    datasources: Datasource[];
};

function NodeEditor({ state, dispatch, datasources }: NodeEditorProps) {
    const { graph, selection } = state;

    const selectedNode = graph.nodes.get(selection.firstNodeId!)!;

    const items = useMemo(() => {
        return datasources
            .filter(item =>
                [ DatasourceType.postgresql, DatasourceType.mongodb, DatasourceType.neo4j ].includes(item.type),
            );
    }, [ datasources ]);

    const datasource = selectedNode.datasource;

    function setDatasource(datasource: Datasource) {
        // TODO Update adaptation settings and graph
    }

    return (<>
        <h3 className='mb-2 text-lg font-semibold'>Edit Kind</h3>

        <div className='text-sm font-semibold text-foreground-400'>Label</div>
        {selectedNode.objex.metadata.label}

        <div className='mt-2'>
            <Select
                items={items}
                label='Datasource'
                labelPlacement='outside'
                classNames={{ label: 'text-sm font-semibold !text-foreground-400' }}
                size='sm'
                placeholder='Select datasource'
                selectedKeys={datasource ? [ datasource.id ] : []}
                renderValue={items => items.map(item => (
                    <div key={item.key} className='flex items-center gap-2'>
                        <DatasourceBadge type={item.data!.type} isCompact />
                        {item.data!.label}
                    </div>
                ))}
            >
                {item => (
                    <SelectItem key={item.id} onPress={() => setDatasource(item)} textValue={item.label}>
                        <div className='flex items-center gap-2'>
                            <DatasourceBadge type={item.type} isCompact />
                            {item.label}
                        </div>
                    </SelectItem>
                )}
            </Select>
        </div>


        {/* <div className='mb-1 text-sm font-semibold text-foreground-400'>Datasource</div>
                    {selectedNode.datasource && (
                        <div className='mb-2 flex items-center gap-2'>
                            <DatasourceBadge type={selectedNode.datasource.type} isFullName />
                        </div>
                    )} */}

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

type EdgeEditorProps = {
    state: AdaptationSettingsState;
    dispatch: AdaptationSettingsDispatch;
};

function EdgeEditor({ state, dispatch }: EdgeEditorProps) {
    const { graph, selection } = state;

    const selectedEdge = graph.edges.get(selection.firstEdgeId!)!;
    const morphism = state.adaptation.settings.morphisms.get(getEdgeSignature(selectedEdge.id))!;

    function setMorphism(edit: Partial<Omit<AdaptationMorphism, 'signature'>>) {

    }

    return (<>
        <h3 className='mb-2 text-lg font-semibold'>Edit Relationship</h3>

        <h4 className='mb-1 text-sm font-semibold text-foreground-400'>Allowed operations</h4>
        <Checkbox isSelected={morphism.isReferenceAllowed} onValueChange={value => setMorphism({ isReferenceAllowed: value })}>
            Reference
        </Checkbox>

        <Checkbox isSelected={morphism.isEmbeddingAllowed} onValueChange={value => setMorphism({ isEmbeddingAllowed: value })}>
            Embedding
        </Checkbox>

        <Checkbox isSelected={morphism.isInliningAllowed} onValueChange={value => setMorphism({ isInliningAllowed: value })}>
            Inlining
        </Checkbox>
    </>);
}
