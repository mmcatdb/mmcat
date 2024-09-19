import { CommonPage } from '@/components/CommonPage';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseList } from '@/components/adminer/DatabaseList';
//import { DatabaseGraph } from '@/components/adminer/DatabaseGraph';

export function AdminerPage() {
    return (
        <CommonPage>
            <div className='mt-5'>
                <DatabaseTable apiUrl="http://localhost:3201/api/v1/mongo/table?name=customer"/>
            </div>
            
            <div className='mt-5'>
                <DatabaseList apiUrl="http://localhost:3201/api/v1/mongo/table?name=customer"/>
            </div>
        </CommonPage>
    );
}
