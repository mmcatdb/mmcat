import { api } from '@/api';
import { type Id } from '@/types/id';
import { Session } from '@/types/job';
import cookies from '@/types/utils/cookies';
import { Button, Select, SelectItem, type SharedSelection } from '@heroui/react';
import { useCallback, useEffect, useState } from 'react';
import { useCategoryInfo } from './CategoryInfoProvider';

const SESSION_COOKIE_NAME = 'session';

/**
 * Dropdown to to select a session, and button to create a new session, for a schema category.
 */
// The functionality should be partially moved to Backend in the future.
export function SessionSelect() {
    const categoryId = useCategoryInfo().category.id;
    const [ sessions, setSessions ] = useState<Session[]>([]);
    const [ selected, setSelected ] = useState<Session>();

    const fetchSessions = useCallback(async (categoryId: Id) => {
        const response = await api.jobs.getAllSessionsInCategory({ categoryId });
        if (!response.status) {
            // TODO handle error
            return;
        }

        const fetchedSession = response.data.map(Session.fromServer).sort((a, b) => +a.createdAt - +b.createdAt);
        setSessions(fetchedSession);

        const cookieId = cookies.get(SESSION_COOKIE_NAME);

        const sessionFromCookie = fetchedSession.find(s => s.id === cookieId);
        if (sessionFromCookie) {
            setSelected(sessionFromCookie);
            return;
        }

        const defaultSession = fetchedSession[0];
        setSelected(defaultSession);
        cookies.set(SESSION_COOKIE_NAME, defaultSession.id);
    }, []);

    useEffect(() => {
        void fetchSessions(categoryId);
    }, [ categoryId, fetchSessions ]);

    async function createSession() {
        const result = await api.jobs.createSession({ categoryId });
        if (!result.status) {
            // TODO handle error
            return;
        }

        const session = Session.fromServer(result.data);
        setSessions([ session, ...sessions ]);
        setSelected(session);
        cookies.set(SESSION_COOKIE_NAME, session.id);
    }

    const selectSession = useCallback((keys: SharedSelection) => {
        if (!sessions)
            return;

        const id = (keys as Set<Id>).values().next().value!;
        const newSession = sessions.find(s => s.id === id);
        if (!newSession)
            return;

        setSelected(newSession);
        cookies.set(SESSION_COOKIE_NAME, id);
    }, [ sessions ]);

    return (
        // Hidden visibility, the session is needed for the app to work,
        // but for now the user does not need it.
        <div className='h-12 items-center gap-3 invisible'>
            <Select
                label='Session'
                defaultSelectedKeys={selected ? [ selected.id ] : []}
                onSelectionChange={selectSession}
                disallowEmptySelection
                size='sm'
                className='w-[320px]'
            >
                {sessions.map(session => (
                    <SelectItem key={session.id}>
                        {session.id}
                    </SelectItem>
                ))}
            </Select>
            <Button onClick={createSession}>
                New session
            </Button>
        </div>
    );
}
