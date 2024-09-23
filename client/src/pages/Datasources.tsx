import { CommonPage } from '@/components/CommonPage';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import AddDatasourceForm from '@/components/datasources/DatasourceForm';

export function DatasourcesPage() {
    return (
        <CommonPage>
            <div className='flex items-center justify-between'>
                <h1 className='text-3xl font-bold leading-tight'>Datasources</h1>
                <AddDatasourceForm />
            </div>

            <div className='mt-5'>
                <DatasourcesTable />
            </div>
        </CommonPage>
    );
}
