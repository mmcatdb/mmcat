import { type SchemaCategory } from '@/types/schema';
import { GraphDisplay } from '../GraphDisplay';
import { useMemo } from 'react';

type SchemaCategoryGraphProps = Readonly<{
    category: SchemaCategory;
}>;

export function SchemaCategoryGraph({ category }: SchemaCategoryGraphProps) {
    const graphData = useMemo(() => ({
        nodes: category.getObjects().map(object => ({
            id: '' + object.key.value,
            label: object.metadata.label,
            position: { ...object.metadata.position },
        })),
        edges: category.getMorphisms().map(morphism => ({
            id: '' + morphism.signature.baseValue,
            label: morphism.metadata.label,
            from: '' + morphism.current?.domKey.value,
            to: '' + morphism.current?.codKey.value,
        })),
    }), [ category ]);

    return (
        <GraphDisplay nodes={graphData.nodes} edges={graphData.edges} width={1200} height={600} />
    );
}
