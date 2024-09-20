import { CommonPage } from '@/components/CommonPage';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseList } from '@/components/adminer/DatabaseList';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    return (
        <CommonPage>
            <div className='mt-5'>
                <DatabaseTable apiUrl={`${BACKEND_API_URL}/postgre/table?name=product`}/>
            </div>
            
            <div className='mt-5'>
                <DatabaseList apiUrl={`${BACKEND_API_URL}/mongo/table?name=customer`} />
            </div>
        </CommonPage>
    );
}
