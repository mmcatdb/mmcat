import { type Datasource } from '@/types/datasource';
import { type DatasourceNode, type FilterNode, type JoinNode, type MinusNode, type OptionalNode, type PatternNode, QueryNodeType, type UnionNode, type QueryNode, type PatternObject } from '@/types/query';
import { capitalize, cn } from '../utils';
import { Fragment } from 'react/jsx-runtime';

type QueryTreeDisplayProps = Readonly<{
    tree: QueryNode;
    datasources: Datasource[];
}>;

export function QueryTreeDisplay({ tree, datasources }: QueryTreeDisplayProps) {
    return (
        <div>
            {nodeDisplay(tree, datasources)}
        </div>
    );
}

const displays = {
    [QueryNodeType.Datasource]: DatasourceNodeDisplay,
    [QueryNodeType.Pattern]: PatternNodeDisplay,
    [QueryNodeType.Join]: JoinNodeDisplay,
    [QueryNodeType.Filter]: FilterNodeDisplay,
    [QueryNodeType.Minus]: MinusNodeDisplay,
    [QueryNodeType.Optional]: OptionalNodeDisplay,
    [QueryNodeType.Union]: UnionNodeDisplay,
} as const;

type NodeDisplayProps<TNode extends QueryNode> = Readonly<{
    node: TNode;
    datasources: Datasource[];
}>;

function nodeDisplay(node: QueryNode, datasources: Datasource[]) {
    const Display = displays[node.type];

    // @ts-expect-error We know that the type is correct. It would be a lot of pain to convince TypeScript.
    return <Display node={node} datasources={datasources} />;
}

function DatasourceNodeDisplay({ node, datasources }: NodeDisplayProps<DatasourceNode>) {
    const datasource = datasources.find(ds => ds.id === node.datasourceIdentifier);

    return (
        <div className='flex flex-col items-center gap-2'>
            <div className='px-2 pb-1 border'>
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
            </div>

            {nodeDisplay(node.child, datasources)}
        </div>
    );
}

function PatternNodeDisplay({ node }: NodeDisplayProps<PatternNode>) {
    return (
        <div className='px-2 pb-1 border'>
            {title(node)}

            <div className='divide-y'>
                {Object.entries(node.kinds).map(([ key, patternObject ]) => (
                    <div key={key} className='p-2 font-mono'>
                        <div className='font-semibold text-gray-500'>{key}</div>
                        <PatternObjectDisplay pattern={patternObject} />
                    </div>
                ))}
            </div>
        </div>
    );
}

function PatternObjectDisplay({ pattern }: { pattern: PatternObject }) {
    const children = Object.entries(pattern.children);

    return (
        <div>
            <div>
                <span className='mr-2 font-semibold'>{pattern.objexKey}:</span>
                {pattern.term}
            </div>

            {children.length > 0 && (
                <div className='pl-8'>
                    {children.map(([ key, child ]) => (
                        <PatternObjectDisplay key={key} pattern={child} />
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

                {node.candidate}
            </div>

            <div className='w-full h-[2px] bg-gray-700' />

            <div className='flex gap-4'>
                {nodeDisplay(node.fromChild, datasources)}

                {nodeDisplay(node.toChild, datasources)}
            </div>
        </div>
    );
}

function FilterNodeDisplay({ node, datasources }: NodeDisplayProps<FilterNode>) {
    return (
        <div>
            <div className='px-2 p-1 border'>
                {title(node)}
            </div>

            <div className='whitespace-pre-wrap font-mono bg-gray-800'>
                {JSON.stringify(node, undefined, 4)}
            </div>

            {nodeDisplay(node.child, datasources)}
        </div>
    );
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
