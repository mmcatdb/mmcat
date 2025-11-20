import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@heroui/react';
import { useMemo } from 'react';

type QueryExampleSelectProps = {
    queryString: string;
    onSelect: (queryString: string) => void;
};

export function QueryExampleSelect({ queryString, onSelect }: QueryExampleSelectProps) {
    const disabledKeys = useMemo(() => {
        return Object.entries(examples)
            .filter(([ , { queryString: qs } ]) => qs === queryString)
            .map(([ key ]) => key);
    }, [ queryString ]);

    return (
        <Dropdown>
            <DropdownTrigger>
                <Button>Example</Button>
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

const examples: Record<string, { label: string, queryString: string }> = {
    basic: { label: 'Basic', queryString:
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
}` },
    join: { label: 'Join', queryString:
`SELECT {
    ?item quantity ?quantity ;
        street ?street .
}
WHERE {
    ?item 53 ?quantity ;
        51/41/42 ?street .
}` },
    filter: { label: 'Filter', queryString:
`SELECT {
    ?order number ?number .
}
WHERE {
    ?order 1 ?number .

    FILTER(?number = "o_100")
}` },
};
