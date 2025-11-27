import { useMemo } from 'react';
import { Card } from '@heroui/react';
import { DatasourceBadge } from '@/components/datasource/DatasourceBadge';
import { categoryToKindGraph } from '@/components/adaptation/kindGraph';
import { type AdaptationInput, type AdaptationResult } from '@/components/adaptation/adaptation';
import { KindGraphDisplay } from '@/components/adaptation/KindGraphDisplay';
import { type Category } from '@/types/schema';
import { useKindGraph } from '@/components/adaptation/useKindGraph';
import { ArrowLongRightIcon } from '@heroicons/react/24/outline';

type AdaptationGraphProps = {
    category: Category;
    adaptation: AdaptationInput | AdaptationResult;
};

export function AdaptationGraph({ category, adaptation }: AdaptationGraphProps) {
    const graph = useMemo(() => categoryToKindGraph(category, adaptation.kinds), [ category, adaptation ]);
    const { selection, dispatch } = useKindGraph();

    const selectedNode = selection?.firstNodeId ? graph.nodes.get(selection.firstNodeId) : undefined;

    return (
        <div className='grid grid-cols-4 gap-4'>
            <Card className='col-span-3 relative'>
                {'price' in adaptation && (
                    <div className='absolute left-1 top-1 px-2 py-1 rounded-lg font-semibold z-50 bg-black'>
                        {adaptation.price}
                    </div>
                )}

                <KindGraphDisplay graph={graph} selection={selection} dispatch={dispatch} className='h-[300px]' />
            </Card>

            {selectedNode && (
                <Card className='p-4'>
                    <h3 className='mb-2 text-lg font-semibold'>{selectedNode.objex.metadata.label}</h3>

                    {selectedNode.kind && (
                        <div className='mb-2 flex items-center gap-2'>
                            <DatasourceBadge type={selectedNode.kind.type} />

                            {selectedNode.adaptation && (<>
                                <ArrowLongRightIcon className='size-5' />
                                <DatasourceBadge type={selectedNode.adaptation.kind.type} />
                            </>)}
                        </div>
                    )}
                    {selectedNode.adaptation && (<>
                        <div className='text-sm font-semibold text-foreground-400'>Improvement</div>
                        <div className='mb-2 font-semibold'>
                            {Math.round(100 * selectedNode.adaptation.improvement)} %
                        </div>
                    </>)}

                    <div className='text-sm font-semibold text-foreground-400'>Properties</div>
                    <ul className='pl-5 list-disc'>
                        {selectedNode.properties.values().map(value => (
                            <li key={value.key.toString()} className=''>
                                {value.metadata.label}
                            </li>
                        ))}
                    </ul>
                </Card>
            )}
        </div>
    );
}
