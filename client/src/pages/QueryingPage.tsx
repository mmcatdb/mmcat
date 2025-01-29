import { api } from '@/api';
import { QueryTreeDisplay } from '@/components/querying/QueryTreeDisplay';
import { type Datasource } from '@/types/datasource';
import { type QueryDescription } from '@/types/query';
import { Button, Textarea } from '@nextui-org/react';
import { useState } from 'react';
import { type Params, useLoaderData, useParams } from 'react-router-dom';

export function QueryingPage() {
    const categoryId = useParams<'categoryId'>().categoryId!;

    const { datasources } = useLoaderData() as { datasources: Datasource[] };

    const [ query, setQuery ] = useState<string>('');
    const [ error, setError ] = useState<unknown>();
    const [ description, setDescription ] = useState<QueryDescription>();

    async function describe() {
        const response = await api.queries.describe({}, { categoryId, queryString: query });
        if (!response.status) {
            setError(response.error);
            setDescription(undefined);
            return;
        }

        setError(undefined);
        setDescription(response.data);
    }

    return (
        <div className='space-y-6'>
            <h2>Querying for Project {categoryId}</h2>

            <Textarea
                className='font-mono'
                label='Query'
                labelPlacement='outside'
                placeholder='Enter your query ...'
                value={query}
                onChange={e => setQuery(e.target.value)}
                disableAnimation
                maxRows={Infinity}
            />

            <div className='text-right space-x-2'>
                <Button color='default' onPress={() => setQuery(exampleBasic)}>
                    Basic Example
                </Button>

                <Button color='default' onPress={() => setQuery(exampleJoin)}>
                    Join Example
                </Button>

                <Button color='default' onPress={() => setQuery(exampleFilter)}>
                    Filter Example
                </Button>

                <Button color='primary' onPress={describe}>
                    Describe
                </Button>
            </div>

            {!!error && (
                <div className='p-4 rounded-lg border border-red-500 bg-default-100 whitespace-pre-wrap break-words font-mono text-red-500'>
                    {JSON.stringify(error, undefined, 4)}
                </div>
            )}

            {description && (
                <QueryTreeDisplay datasources={datasources} tree={description.tree} />
            )}

            {description && (
                <div className='p-4 border whitespace-pre-wrap font-mono'>
                    {JSON.stringify(description.parts, undefined, 4)}
                </div>
            )}
        </div>
    );
}

async function loader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID is required');

    const response = await api.datasources.getAllDatasources({}, { categoryId: categoryId });

    if (!response.status)
        throw new Error('Failed to load schema category');

    return {
        datasources: response.data,
    };
}

QueryingPage.loader = loader;

const exampleBasic =
`SELECT {
    ?product
        productId ?id ;
        name ?label ;
        totalPrice ?price .
}
WHERE {
    ?product
        15 ?id ;
        16 ?label ;
        17 ?price .
}`;

const exampleJoin =
`SELECT {
    ?item quantity ?quantity ;
        street ?street .
}
WHERE {
    ?item 14 ?quantity ;
        12/8/9 ?street .
}`;


const exampleFilter =
`SELECT {
    ?order number ?number .
}
WHERE {
    ?order 1 ?number .

    FILTER(?number = "o_100")
}`;
