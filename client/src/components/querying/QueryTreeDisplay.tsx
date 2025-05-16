import { type Datasource } from '@/types/datasource';
import { type DatasourceNode, type FilterNode, type JoinNode, type MinusNode, type OptionalNode, QueryNodeType, type UnionNode, type QueryNode, type PatternTree, type JoinCandidate } from '@/types/query';
import { capitalize, cn } from '../utils';
import { Fragment } from 'react/jsx-runtime';
import { ArrowLongRightIcon } from '@heroicons/react/24/outline';

type QueryTreeDisplayProps = {
    tree: QueryNode;
    datasources: Datasource[];
};

export function QueryTreeDisplay({ tree, datasources }: QueryTreeDisplayProps) {
    return (
        <div>
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
            <div className='px-2 pt-1 border'>
                {title(node)}

                {datasource ? (
                    <div>
                        {datasource.label}
                    </div>
                ) : (
                    <div className='text-red-500'>
                        Datasource not found<br/>
                        {node.datasourceIdentifier}
                    </div>
                )}


                <div className='divide-y'>
                    {Object.entries(node.kinds).map(([ key, patternTree ]) => (
                        <div key={key} className='py-2 font-mono'>
                            <div className='font-semibold text-gray-500'>{key}</div>
                            <PatternTreeDisplay pattern={patternTree} />
                        </div>
                    ))}
                </div>

                <div className='divide-y'>
                    {node.joinCandidates.map((candidate, index) => (
                        <JoinCandidateDisplay key={index} candidate={candidate} />
                    ))}

                    {node.filters.map((filter, index) => (
                        <div key={index}>
                            {filter}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

function PatternTreeDisplay({ pattern, signature }: { pattern: PatternTree, signature?: string }) {
    const children = Object.entries(pattern.children);

    return (
        <div>
            <div>
                {signature && (
                    <span className='mr-2 font-semibold'>{signature}:</span>
                )}
                <span className='mr-2'>
                    {pattern.term}
                </span>
                (<span className='italic'>{pattern.objexKey}</span>)
            </div>

            {children.length > 0 && (
                <div className='pl-8'>
                    {children.map(([ signature, child ]) => (
                        <PatternTreeDisplay key={signature} pattern={child} signature={signature} />
                    ))}
                </div>
            )}
        </div>
    );
}

function JoinNodeDisplay({ node, datasources }: NodeDisplayProps<JoinNode>) {
    return (
        <div className='w-fit flex flex-col items-center gap-2'>
            <div className='px-2 pb-1 border'>
                {title(node)}

                <JoinCandidateDisplay candidate={node.candidate} />
            </div>

            <div className='w-full h-[2px] bg-gray-700' />

            <div className='flex gap-4'>
                {nodeDisplay(node.fromChild, datasources)}

                {nodeDisplay(node.toChild, datasources)}
            </div>
        </div>
    );
}

function JoinCandidateDisplay({ candidate }: { candidate: JoinCandidate }) {
    return (
        <div className='spyce-y-2 font-mono'>
            <div className='flex gap-2 leading-5'>
                <span className='font-semibold text-gray-500'>{candidate.fromKind}</span>
                <ArrowLongRightIcon className='size-5' />
                <span className='font-semibold text-gray-500'>{candidate.toKind}</span>
                <span className='font-sans'>{`(${candidate.type})`}</span>
            </div>

            <div className='flex gap-2 leading-5 font-semibold'>
                {candidate.fromPath}
                <ArrowLongRightIcon className='size-5' />
                {candidate.toPath}
            </div>
        </div>
    );
}

function FilterNodeDisplay({ node, datasources }: NodeDisplayProps<FilterNode>) {
    return (<>
        <div className='mb-4 flex flex-col items-center'>
            <div className='px-2 p-1 border'>
                {title(node)}

                {node.filter}
            </div>
        </div>

        {nodeDisplay(node.child, datasources)}
    </>);
}

function MinusNodeDisplay({ node, datasources }: NodeDisplayProps<MinusNode>) {
    return (
        <div>
            <div className='px-2 p-1 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}
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
            <div className='px-2 p-1 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}
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
            <div className='px-2 p-1 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}
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
    return <h4 className={cn('px-3 py-1 text-center font-semibold', className)}>{capitalize(type)}</h4>;
}
