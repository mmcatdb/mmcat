import { DATASOURCE_MODELS, DatasourceType } from '@/types/Datasource';
import { SiMongodb, SiNeo4J, SiPostgresql } from 'react-icons/si';
import { type IconType } from 'react-icons/lib';
import { BsFileEarmark, BsFiletypeCsv, BsFiletypeJson } from 'react-icons/bs';
import { cn } from '../utils';

type DatasourceBadgeProps = {
    type: DatasourceType;
    isFullName?: boolean;
    isCompact?: boolean;
    className?: string;
};

export function DatasourceBadge({ type, isFullName, isCompact, className }: DatasourceBadgeProps) {
    const { icon, label } = datasources[type];
    const model = DATASOURCE_MODELS[type];

    return (
        <div className={cn('h-6 w-fit flex items-center gap-2 rounded-full text-black font-medium', isCompact ? 'w-6 justify-center' : 'px-1', className)} title={label} style={{ backgroundColor: `var(--mm-${model}-light)` }}>
            {icon({ size: 20, className: 'text-black' })}

            {isFullName && label}

            {!isCompact && (
                <div className='size-4 rounded-full' style={{ backgroundColor: `var(--mm-${model}-dark)` }} />
            )}
        </div>
    );
}

type DatasourceIconProps = {
    type: DatasourceType;
    size?: number;
    className?: string;
};

export function DatasourceIcon({ type, size, className }: DatasourceIconProps) {
    const { icon, label } = datasources[type];

    return icon({ size: size ?? 20, title: label, className });
}

const datasources: Record<DatasourceType, {
    label: string;
    icon: IconType;
}> = {
    [DatasourceType.mongodb]: { label: 'MongoDB', icon: SiMongodb },
    [DatasourceType.postgresql]: { label: 'PostgreSQL', icon: SiPostgresql },
    [DatasourceType.neo4j]: { label: 'Neo4j', icon: SiNeo4J },
    [DatasourceType.csv]: { label: 'CSV', icon: BsFiletypeCsv },
    [DatasourceType.json]: { label: 'JSON', icon: BsFiletypeJson },
    // This icon means that we don't have a specific icon for this model.
    [DatasourceType.jsonld]: { label: 'JSON-LD', icon: BsFileEarmark },
};
