import { useState } from 'react';
import { PageLayout } from '@/components/RootLayout';
import { Button } from '@heroui/react';
import { api } from '@/api';

export function DevPage() {
    const [ isPingFetching, setIsPingFetching ] = useState(false);
    const [ pingResponse, setPingResponse ] = useState<string>();

    async function ping() {
        setIsPingFetching(true);
        const response = await api.dev.ping({});
        setIsPingFetching(false);

        if (response.status)
            setPingResponse(response.data);
    }

    return (
        <PageLayout isFullscreen className='px-8 pt-4 pb-12 flex flex-col gap-4'>
            <h1>You are developer, Harry!</h1>

            <div className='flex items-center gap-4'>
                <Button onPress={ping} isDisabled={isPingFetching}>
                    Ping!
                </Button>

                {pingResponse && (
                    <div>{pingResponse}</div>
                )}
            </div>
        </PageLayout>
    );
}
