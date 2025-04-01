import { Divider } from '@nextui-org/react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import type { Datasource } from '@/types/datasource/Datasource';
import type { DocumentResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseDocumentProps = Readonly<{
    fetchedData: DocumentResponse | GraphResponse;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseDocument({ fetchedData, kindReferences, kind, datasourceId, datasources }: DatabaseDocumentProps) {
    if (fetchedData && fetchedData.data.length > 1) {
        return (
            <div>
                {fetchedData.data.map((value, index) =>
                    <div key={index}>
                        <DocumentComponent valueKey={null} value={value} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources}/>

                        {(index != fetchedData.data.length - 1) && <Divider className='my-4'/> }
                    </div>,
                )}
            </div>
        );
    }

    return (
        <div>
            {fetchedData && fetchedData.data.length > 0 ? (
                <DocumentComponent valueKey={null} value={fetchedData.data} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
}
