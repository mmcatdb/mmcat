import { useState } from 'react';
import { routes } from '@/routes/routes';
import { Link } from 'react-router-dom';
import { AiFillBug, AiOutlineBug } from 'react-icons/ai';
import { Button } from '@heroui/react';
import { cn } from './common/utils';

export function DevBar() {
    const [ show, setShow ] = useState(false);

    return (<>
        <div className='fixed bottom-4 right-[96px] z-9999999999 flex justify-end pointer-events-none'>
            <div className={cn('h-12 px-1 py-1 rounded-medium flex gap-3 items-center pointer-events-auto bg-danger-300', !show && 'hidden')}>
                <Button as={Link} to={routes.dev}>Dev page</Button>

                <div className='pr-2'>
                    Place for custom dev functions.
                </div>
            </div>
            <div className='h-0 w-0 pointer-events-auto'>
                <div className='fixed right-[24px] bottom-5'>
                    <Button color={show ? 'success' : 'danger'} className='min-w-10' onPress={() => setShow(prev => !prev)}>
                        {show ? (
                            <AiOutlineBug size={24} />
                        ) : (
                            <AiFillBug size={24} />
                        )}
                    </Button>
                </div>
            </div>
        </div>
    </>);
}
