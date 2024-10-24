import { type SchemaCategory } from '@/types/schema';
import ReactFlow from 'reactflow';
import { GraphDisplay } from '../GraphDisplay';
import { useMemo } from 'react';

type SchemaCategoryGraphProps = Readonly<{
    category: SchemaCategory;
}>;

export function SchemaCategoryGraph({ category }: SchemaCategoryGraphProps) {
    const initialNodes = category.getObjects().map(object => ({
        id: '' + object.key.value,
        data: { label: object.current?.label },
        position: object._position,
    }));

    const initialEdges = category.getMorphisms().map(morphism => ({
        id: '' + morphism.signature.baseValue,
        source: '' + morphism.current?.domKey.value,
        target: '' + morphism.current?.codKey.value,
        data: { label: morphism.current?.label },
    }));

    console.log({ initialNodes, initialEdges });

    const graphData = useMemo(() => ({
        nodes: initialNodes.map(node => ({
            id: node.id,
            label: node.data.label ?? '',
            position: { ...node.position },
        })),
        edges: initialEdges.map(edge => ({
            id: edge.id,
            label: edge.data.label ?? '',
            from: edge.source,
            to: edge.target,
        })),
    }), []);

    return (<>
        {/* <div className='w-[1200px] h-[400px] bg-slate-700'>
            <ReactFlow nodes={initialNodes} edges={initialEdges} />
        </div> */}

        <GraphDisplay nodes={graphData.nodes} edges={graphData.edges} width={1200} height={800} />
    </>);
}
