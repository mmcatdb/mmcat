import { useFetchData } from '@/components/adminer/useFetchData';
import { type AdminerStateAction } from '@/types/adminer/Reducer';
import { Spinner, Button } from '@nextui-org/react';

type KindMenuProps = Readonly<{
    apiUrl: string;
    kindName: string | undefined;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function KindMenu({ apiUrl, kindName, dispatch }: KindMenuProps) {
    const { fetchedData, loading, error } = useFetchData(apiUrl);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;


    return (
        <div className='flex flex-wrap gap-3 items-center'>
            {fetchedData && fetchedData.data.length > 0 ? (
                fetchedData.data.map((name, index) => (
                    <Button
                        key={index}
                        onPress={() =>  dispatch({ type:'kind', newKind: name }) }
                        color={kindName === name ? 'primary' : 'default'}
                        variant={kindName === name ? 'solid' : 'ghost'}
                        className='flex-1 min-w-[50px]'
                    >
                        <span className='truncate'>{name}</span>
                    </Button>
                ))
            ) : (
                <span>No tables to display.</span>
            )}
        </div>
    );
}
