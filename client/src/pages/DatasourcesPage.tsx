import { CommonPage } from '@/components/CommonPage';
import { Button } from '@nextui-org/react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';

export function DatasourcesPage() {
    return (
        <CommonPage>
            <div className='flex items-center justify-between'>
                <h1 className='text-3xl font-bold leading-tight'>Datasources</h1>
                <Button startContent={<p>🐢</p>}>Add Datasource</Button>
            </div>

            <div className='mt-5'>
                <DatasourcesTable />
            </div>
        </CommonPage>
    );
}
