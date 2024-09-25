import { CommonPage } from '@/components/CommonPage';
import { DatabaseMenu } from '@/components/adminer/DatabaseMenu';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseList } from '@/components/adminer/DatabaseList';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    return (
        <CommonPage>
            <div className='mt-5'>
                <DatabaseMenu/>
            </div>

            <div className='mt-5'>
                <DatabaseTable apiUrl={`${BACKEND_API_URL}/adminer/6e046c0f-b664-4be1-b0eb-e257a5052951/table?name=product`}/>
            </div>
            
            <div className='mt-5'>
                <DatabaseList apiUrl={`${BACKEND_API_URL}/adminer/0fdc1b15-997f-46a8-a0f7-5e7c2587d8f1/table?name=address`} />
            </div>
        </CommonPage>
    );
}
