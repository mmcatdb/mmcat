import { Input, Button, Spacer } from '@nextui-org/react';
import { useState } from 'react';

type LimitFormProps = Readonly<{
    limit: number;
    setLimit: (limit: number) => void;
}>;

export function LimitForm({ limit, setLimit }: LimitFormProps) {
    const [ actualLimit, setActualLimit ] = useState<number>(limit);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setLimit(actualLimit);
    };

    return (
        <form className='mt-5' onSubmit={handleSubmit} style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <Input
                label='Limit'
                placeholder='Enter limit'
                value={actualLimit.toString()}
                onChange={(e) => setActualLimit(Number(e.target.value))}
                required
            />

            <Spacer y={1.5} />

            <Button type='submit' color='primary'>
                Submit
            </Button>
        </form>
    );
}
