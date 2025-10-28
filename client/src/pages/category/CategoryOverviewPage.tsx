import { Card, CardBody, CardHeader } from '@heroui/react';
import { Button } from '@heroui/react';
import { Link, useLoaderData, type Params } from 'react-router-dom';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { api } from '@/api';
import { type SchemaCategoryStats } from '@/types/schema';
import { routes } from '@/routes/routes';
import { FaDatabase, FaPlus, FaPlay, FaSearch, FaEdit } from 'react-icons/fa';
import { type ReactNode } from 'react';
import { PageLayout } from '@/components/RootLayout';

export function CategoryOverviewPage() {
    const { stats } = useLoaderData() as CategoryLoaderData;
    const { category } = useCategoryInfo();

    const categoryId = category.id;

    return (
        <PageLayout className='space-y-6'>
            {/* Header */}
            <h1 className='text-3xl font-bold text-primary-500 truncate max-w-[1000px]' title={category.label}>{category.label}</h1>

            {/* Overview Cards */}
            <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
                <OverviewCard title='Total Objects' value={stats.objexes} />
                <OverviewCard title='Total Mappings' value={stats.mappings} />
                <OverviewCard title='Jobs' value={stats.jobs} />
                <OverviewCard title='System Version ID' value={category.systemVersionId || 'N/A'} />
            </div>

            {/* Schema Category Explanation */}
            <Card className='shadow-lg'>
                <CardHeader>
                    <h2 className='text-xl font-semibold'>What is a Schema Category?</h2>
                </CardHeader>
                <CardBody className='space-y-4'>
                    <p className='text-default-700'>
                        A <span className='font-semibold'>Schema Category</span> is a high-level model of your data, represented as a graph. It defines objects (data types) and morphisms (relationships) without tying them to specific database structures. Think of it as a conceptual workspace for your data modeling project.
                    </p>
                    <p className='text-default-700'>
                        Within this category, you can:
                    </p>
                    <ul className='list-disc pl-6 space-y-2 text-default-700'>
                        <li>Build a <strong>Schema Category Graph</strong> in the Editor to define objects and relationships.</li>
                        <li>Create <strong>Mappings</strong> to connect your schema to data sources for import/export jobs.</li>
                        <li>Manage <strong>Datasources</strong> (e.g., MongoDB, PostgreSQL) to link external databases.</li>
                        <li>Define <strong>Actions</strong> and run <strong>Jobs</strong> to transform and process data.</li>
                    </ul>
                    <p className='text-default-600 text-sm'>
                        Note: Changes to the graph that could break existing mappings are restricted. Future updates will include procedures to handle such breaking changes safely.
                    </p>
                </CardBody>
            </Card>

            {/* Quick Actions */}
            <div className='space-y-6'>
                <h2 className='text-2xl font-semibold'>Quick Actions</h2>
                <div className='grid grid-cols-1 md:grid-cols-3 gap-6 text-center'>
                    <FeatureCard
                        icon={<FaEdit className='mx-auto size-12 text-primary-500' />}
                        title='Schema Editor'
                        description='Modify objects and relationships in the graph.'
                        linkText='Open Editor'
                        linkTo={routes.category.editor.resolve({ categoryId })}
                    />
                    <FeatureCard
                        icon={<FaDatabase className='mx-auto size-12 text-secondary-500' />}
                        title='Manage Datasources'
                        description='Link databases and define mappings.'
                        linkText='View Datasources'
                        linkTo={routes.category.datasources.list.resolve({ categoryId })}
                    />
                    <FeatureCard
                        icon={<FaPlus className='mx-auto size-12 text-success-500' />}
                        title='Manage Actions'
                        description='Set up data transformation actions and create runs.'
                        linkText='Manage Actions'
                        linkTo={routes.category.actions.list.resolve({ categoryId })}
                    />
                    <FeatureCard
                        icon={<FaPlay className='mx-auto size-12 text-warning-500' />}
                        title='View Jobs'
                        description='Monitor and manage data processing jobs.'
                        linkText='View Jobs'
                        linkTo={routes.category.jobs.resolve({ categoryId })}
                    />
                    <FeatureCard
                        icon={<FaSearch className='mx-auto size-12 text-default-500' />}
                        title='Query Data'
                        description='Explore and query data within this category.'
                        linkText='Query Data'
                        linkTo={routes.category.queries.new.resolve({ categoryId })}
                    />
                </div>
            </div>
        </PageLayout>
    );
}

type CategoryLoaderData = {
    stats: SchemaCategoryStats;
};

CategoryOverviewPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<CategoryLoaderData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const response = await api.schemas.getCategoryStats({ id: categoryId });
    if (!response.status)
        throw new Error('Failed to load category stats');

    return {
        stats: response.data,
    };
};

function OverviewCard({ title, value }: { title: string, value: number | string }) {
    return (
        <Card className='p-4 text-center shadow-lg'>
            <h3 className='text-lg font-semibold text-default-700'>{title}</h3>
            <p className='text-2xl font-bold text-primary-500'>{value}</p>
        </Card>
    );
}

type FeatureCardProps = {
    icon: ReactNode;
    title: string;
    description: string;
    linkText: string;
    linkTo: string;
};

function FeatureCard({ icon, title, description, linkText, linkTo }: FeatureCardProps) {
    return (
        <Card className='p-6 shadow-medium hover:shadow-large transition h-full flex flex-col'>
            {icon}
            <h3 className='mt-4 font-semibold text-lg'>{title}</h3>
            <p className='text-default-600 mb-4'>{description}</p>
            <Button
                as={Link}
                to={linkTo}
                variant='ghost'
                className='mt-auto'
            >
                {linkText}
            </Button>
        </Card>
    );
}
