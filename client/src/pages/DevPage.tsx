import { useState } from 'react';
import { PageLayout } from '@/components/RootLayout';
import { Button } from '@heroui/react';
import { api } from '@/api';

export function DevPage() {
    const [ isPingFetching, setIsPingFetching ] = useState(false);
    const [ pingResponse, setPingResponse ] = useState<string>();

    const [ isTestingSeparate, setIsTestingSeparate ] = useState(false);
    const [ testResponseSeparate, setTestResponseSeparate ] = useState<string>();
    const [ isTestingAll, setIsTestingAll ] = useState(false);
    const [ testResponseAll, setTestResponseAll ] = useState<string>();

    async function ping() {
        setIsPingFetching(true);
        const response = await api.dev.ping({});
        setIsPingFetching(false);

        if (response.status)
            setPingResponse(response.data);
    }

    async function runTestAllDatasources() {
        setIsTestingAll(true);
        const response = await api.dev.runTestAllDatasources({});
        setIsTestingAll(false);

        if (response.status)
            setTestResponseAll(response.data);
    }

    async function runTestSeparateDatasources() {
        setIsTestingSeparate(true);
        const response = await api.dev.runTestSeparateDatasources({});
        setIsTestingSeparate(false);

        if (response.status)
            setTestResponseSeparate(response.data);
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

            <div className='flex items-center gap-4'>
                <Button onPress={runTestSeparateDatasources} isDisabled={isTestingSeparate}>
                    Run Tests (cal.com Schema, separate datasources)
                </Button>

                {testResponseSeparate && (
                    <div>{testResponseSeparate}</div>
                )}
            </div>

            <div className='flex items-center gap-4'>
                <Button onPress={runTestAllDatasources} isDisabled={isTestingAll}>
                    Run Tests (cal.com Schema, all datasources)
                </Button>

                {testResponseAll && (
                    <div>{testResponseAll}</div>
                )}
            </div>
        </PageLayout>
    );
}
