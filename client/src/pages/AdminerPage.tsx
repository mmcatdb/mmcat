import { CommonPage } from '@/components/CommonPage';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';

export function AdminerPage() {
    return (
        <CommonPage>
            <div className='mt-5'>
                <DatabaseTable apiUrl=""/>
            </div>
        </CommonPage>
    );
}
