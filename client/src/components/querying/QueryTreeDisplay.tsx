import { useMemo } from 'react';
import { type Datasource } from '@/types/Datasource';
import { type DatasourceNode, type FilterNode, type JoinNode, type MinusNode, type OptionalNode, QueryNodeType, type UnionNode, type QueryNode, type JoinCandidate, ResultStructure } from '@/types/query';
import { capitalize } from '@/types/utils/common';
import { Fragment } from 'react/jsx-runtime';
import { ArrowLongRightIcon } from '@heroicons/react/24/outline';
import { cn } from '@/components/common/utils';
import { ResultStructureDisplay } from './ResultStructureDisplay';
import { DatasourceBadge, datasourceCssColor } from '../datasource/DatasourceBadge';
import { PatternTreeDisplay } from './PatternTreeDisplay';

type QueryTreeDisplayProps = {
    tree: QueryNode;
    datasources: Datasource[];
    className?: string;
};

export function QueryTreeDisplay({ tree, datasources, className }: QueryTreeDisplayProps) {
    return (
        <div className={cn('flex justify-center', className)}>
            {nodeDisplay(tree, datasources)}
        </div>
    );
}

const displays = {
    [QueryNodeType.Datasource]: DatasourceNodeDisplay,
    [QueryNodeType.Join]: JoinNodeDisplay,
    [QueryNodeType.Filter]: FilterNodeDisplay,
    [QueryNodeType.Minus]: MinusNodeDisplay,
    [QueryNodeType.Optional]: OptionalNodeDisplay,
    [QueryNodeType.Union]: UnionNodeDisplay,
} as const;

type NodeDisplayProps<TNode extends QueryNode> = {
    node: TNode;
    datasources: Datasource[];
};

function nodeDisplay(node: QueryNode, datasources: Datasource[]) {
    const Display = displays[node.type];

    // @ts-expect-error We know that the type is correct. It would be a lot of pain to convince TypeScript.
    return <Display node={node} datasources={datasources} />;
}

function DatasourceNodeDisplay({ node, datasources }: NodeDisplayProps<DatasourceNode>) {
    const datasource = datasources.find(ds => ds.id === node.datasourceIdentifier);

    return (
        <div className='flex flex-col items-center'>
            <div className='p-2 border'>
                {title(node)}

                {datasource ? (
                    <div className='flex items-center gap-2'>
                        <DatasourceBadge type={datasource.type} />
                        {datasource.label}
                    </div>
                ) : (
                    <div className='text-red-500'>
                        Datasource not found<br />
                        {node.datasourceIdentifier}
                    </div>
                )}

                <div className='-mx-2 mt-2 flex divide-x'>
                    {Object.entries(node.kinds).map(([ key, patternTree ]) => (
                        <div key={key} className='px-2'>
                            <div className='font-semibold font-mono' style={{ color: datasource && datasourceCssColor(datasource.type) }}>{key}</div>
                            <div className='text-sm font-semibold text-foreground-400'>Pattern</div>
                            <PatternTreeDisplay pattern={patternTree} />
                        </div>
                    ))}
                </div>

                <div className='mt-2 divide-y empty:hidden'>
                    {node.joinCandidates.map((candidate, index) => (
                        <JoinCandidateDisplay key={index} candidate={candidate} datasources={datasources} />
                    ))}

                    {node.filters.map((filter, index) => (
                        <div key={index} className='font-mono'>
                            {filter}
                        </div>
                    ))}
                </div>

                <Structure node={node} className='mt-2' />
            </div>
        </div>
    );
}

function JoinNodeDisplay({ node, datasources }: NodeDisplayProps<JoinNode>) {
    return (
        <div className='w-fit flex flex-col items-center gap-2'>
            <div className='p-2 border'>
                {title(node)}

                <JoinCandidateDisplay candidate={node.candidate} datasources={datasources} />

                <Structure node={node} className='mt-1' />
            </div>

            <div className='w-full h-[2px] bg-gray-700' />

            <div className='flex gap-4'>
                {nodeDisplay(node.fromChild, datasources)}

                {nodeDisplay(node.toChild, datasources)}
            </div>
        </div>
    );
}

type JoinCandidateDisplayProps = {
    candidate: JoinCandidate;
    datasources: Datasource[];
};

function JoinCandidateDisplay({ candidate, datasources }: JoinCandidateDisplayProps) {
    const { fromDatasource, toDatasource } = useMemo(() => ({
        fromDatasource: datasources.find(ds => ds.id === candidate.from.datasourceIdentifier),
        toDatasource: datasources.find(ds => ds.id === candidate.to.datasourceIdentifier),
    }), [ candidate, datasources ]);

    return (
        <div className='font-mono'>
            <div className='flex gap-2 leading-5'>
                <span className='font-semibold' style={{ color: fromDatasource && datasourceCssColor(fromDatasource.type) }}>{candidate.from.kindName}</span>
                <ArrowLongRightIcon className='size-5' />
                <span className='font-semibold' style={{ color: toDatasource && datasourceCssColor(toDatasource.type) }}>{candidate.to.kindName}</span>
                <span className='font-sans'>{`(${candidate.type})`}</span>
            </div>

            <div className='flex gap-2 leading-5 font-semibold'>
                {candidate.from.path}
                <ArrowLongRightIcon className='size-5' />
                {candidate.to.path}
            </div>
        </div>
    );
}

function FilterNodeDisplay({ node, datasources }: NodeDisplayProps<FilterNode>) {
    return (<>
        <div className='mb-4 flex flex-col items-center'>
            <div className='p-2 border'>
                {title(node)}

                {node.filter}

                <Structure node={node} />
            </div>
        </div>

        {nodeDisplay(node.child, datasources)}
    </>);
}

function MinusNodeDisplay({ node, datasources }: NodeDisplayProps<MinusNode>) {
    return (
        <div>
            <div className='p-2 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}

                {/* TODO */}
            </div>

            <div className='w-full h-[2px] bg-gray-700' />

            <div className='flex gap-4'>
                {nodeDisplay(node.primaryChild, datasources)}

                {nodeDisplay(node.minusChild, datasources)}
            </div>
        </div>
    );
}

function OptionalNodeDisplay({ node, datasources }: NodeDisplayProps<OptionalNode>) {
    return (
        <div>
            <div className='p-2 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}

                {/* TODO */}
            </div>

            <div className='w-full h-[2px] bg-gray-700' />

            <div className='flex gap-4'>
                {nodeDisplay(node.primaryChild, datasources)}

                {nodeDisplay(node.optionalChild, datasources)}
            </div>
        </div>
    );
}

function UnionNodeDisplay({ node, datasources }: NodeDisplayProps<UnionNode>) {
    return (
        <div>
            <div className='p-2 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}

                {/* TODO */}
            </div>

            <div className='w-full h-[2px] bg-gray-700' />

            <div className='flex gap-4'>
                {node.children.map((child, index) => (
                    <Fragment key={index}>
                        {nodeDisplay(child, datasources)}
                    </Fragment>
                ))}
            </div>
        </div>
    );
}

function title({ type }: QueryNode, className?: string) {
    return <h4 className={cn('mb-2 px-3 text-center font-semibold', className)}>{capitalize(type)}</h4>;
}

function Structure({ node, className }: { node: QueryNode, className?: string }) {
    const structure = useMemo(() => ResultStructure.fromResponse(node.structure), [ node.structure ]);

    return (
        <div className={className}>
            <div className='text-sm font-semibold text-foreground-400'>Result Structure</div>

            <ResultStructureDisplay structure={structure} />
        </div>
    );
}
