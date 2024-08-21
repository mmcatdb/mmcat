import { CommonPage } from '@/components/CommonPage';
import { Button } from '@nextui-org/react';
import { DatabasesTable } from '@/components/databases/DatabasesTable';

export function DatabasesPage() {
    return (
        <CommonPage>
            <div className='flex items-center justify-between'>
                <h1 className='text-3xl font-bold leading-tight'>Databases</h1>
                <Button startContent={<p>🐢</p>}>Add Database</Button>
            </div>

            <div className='mt-5'>
                <DatabasesTable />
            </div>
        </CommonPage>
    );
}
