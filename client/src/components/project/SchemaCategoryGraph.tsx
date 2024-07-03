import { type SchemaCategory } from '@/types/schema';
import ReactFlow from 'reactflow';

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

    return (
        <div className='w-[1200px] h-[800px] bg-slate-700'>
            <ReactFlow nodes={initialNodes} edges={initialEdges} />
        </div>
    );
}
