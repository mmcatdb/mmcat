import { Example } from '@/types/schema';
import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@heroui/react';
import { useMemo } from 'react';
import { FaPlus } from 'react-icons/fa';

type QueryExampleSelectProps = {
    categoryExample: string | undefined;
    queryString: string;
    onSelect: (queryString: string) => void;
};

export function QueryExampleSelect({ categoryExample, queryString, onSelect }: QueryExampleSelectProps) {
    const examples = categoryExample ? (examplesForCategories as Record<string, QueryExamples | undefined>)[categoryExample] : undefined;

    const disabledKeys = useMemo(() => {
        return examples && Object.entries(examples)
            .filter(([ , { queryString: qs } ]) => qs === queryString)
            .map(([ key ]) => key);
    }, [ queryString, examples ]);

    if (!examples)
        return null;

    return (
        <Dropdown>
            <DropdownTrigger>
                <Button color='secondary' variant='flat'><FaPlus className='size-4' />Example</Button>
            </DropdownTrigger>
            <DropdownMenu onAction={key => onSelect(examples[key].queryString)} disabledKeys={disabledKeys}>
                {Object.entries(examples).map(([ key, { label } ]) => (
                    <DropdownItem key={key}>
                        {label}
                    </DropdownItem>
                ))}
            </DropdownMenu>
        </Dropdown>
    );
}

type QueryExamples = Record<string, { label: string, queryString: string }>;

const examplesForCategories: Partial<Record<Example, QueryExamples>> = {
    [Example.basic]: {
        simple: { label: 'Simple', queryString:
`SELECT {
    ?product
        productId ?id ;
        name ?label ;
        totalPrice ?price .
}
WHERE {
    ?product
        54 ?id ;
        55 ?label ;
        56 ?price .
}`,
        },
        join: { label: 'Join', queryString:
`SELECT {
    ?item quantity ?quantity ;
        street ?street .
}
WHERE {
    ?item 53 ?quantity ;
        51/41/42 ?street .
}`,
        },
        filter: { label: 'Filter', queryString:
`SELECT {
    ?order number ?number .
}
WHERE {
    ?order 1 ?number .

    FILTER(?number = "o_100")
}`,
        },
    },
};
