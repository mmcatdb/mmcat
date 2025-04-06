import { Card } from '@nextui-org/react';
import { Button } from '@nextui-org/react';
import { Link, useLoaderData, type Params } from 'react-router-dom';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { api } from '@/api';
import { type SchemaCategoryStats } from '@/types/schema';

export function CategoryOverviewPage() {
    const { stats } = useLoaderData() as CategoryLoaderData;

    const { category } = useCategoryInfo();

    return (
        <div className='p-6 space-y-6'>
            {/* Overview Cards */}
            <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
                <OverviewCard title='Total Objects' value={stats.objects} />
                <OverviewCard title='Total Mappings' value={stats.mappings} />
                <OverviewCard title='Jobs' value={stats.jobs} />
                <OverviewCard title='System Version ID' value={category.systemVersionId || 'N/A'} />
            </div>

            {/* Recent Activity */}
            <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
                <RecentActivity title='Recent Edits' data={[]} />
                <RecentActivity title='Recent Jobs' data={[]} />
            </div>

            {/* Quick Actions */}
            <QuickActions />
        </div>
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
            <h3 className='text-lg font-semibold'>{title}</h3>
            <p className='text-2xl font-bold'>{value}</p>
        </Card>
    );
}

function RecentActivity({ title, data }: { title: string, data: { name: string, date: string, link: string }[] }) {
    return (
        <Card className='p-4'>
            <h3 className='text-lg font-semibold mb-2'>{title}</h3>
            {data.length === 0 ? (
                <p className='text-gray-500'>No recent activity</p>
            ) : (
                <ul className='space-y-2'>
                    {data.map((item, index) => (
                        <li key={index} className='flex justify-between'>
                            <Link to={item.link} className='text-blue-500 hover:underline'>{item.name}</Link>
                            <span className='text-gray-500 text-sm'>{item.date}</span>
                        </li>
                    ))}
                </ul>
            )}
        </Card>
    );
}

function QuickActions() {
    return (
        <Card className='p-4'>
            <h3 className='text-lg font-semibold mb-4'>Quick Actions</h3>
            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <Button as={Link} to='editor' color='primary'>Open Editor</Button>
                <Button as={Link} to='datasources' color='secondary'>View Datasources with mappings</Button>
                <Button as={Link} to='actions' color='success'>Manage Actions</Button>
                <Button as={Link} to='jobs' color='warning'>View Jobs</Button>
            </div>
        </Card>
    );
}
