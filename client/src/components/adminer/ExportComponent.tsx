import { Button } from '@heroui/react';
import type { DataResponse } from '@/types/adminer/DataResponse';

type ExportComponentProps = {
    /** The data to export. */
    data: DataResponse;
};

/**
 * Component for exporting the data into JSON file
 */
export function ExportComponent({ data }: ExportComponentProps) {
    return (
        <Button
            className='min-w-20'
            size='sm'
            color='primary'
            onPress={() => exportJSON(data)}
        >
            Export
        </Button>
    );
}

function exportJSON(data: DataResponse) {
    const json = JSON.stringify(data.data, null, 2);
    const blob = new Blob([ json ], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = 'data.json';
    link.click();

    URL.revokeObjectURL(url);
}
