import CodeMirror, { type Extension, type KeyBinding, keymap } from '@uiw/react-codemirror';
import { lineNumbers, highlightActiveLineGutter, highlightSpecialChars, drawSelection, dropCursor, rectangularSelection, crosshairCursor, highlightActiveLine } from '@codemirror/view';
import { EditorState } from '@codemirror/state';
import { history, defaultKeymap, historyKeymap } from '@codemirror/commands';
import { highlightSelectionMatches, searchKeymap } from '@codemirror/search';
import { closeBrackets, autocompletion, closeBracketsKeymap, completionKeymap } from '@codemirror/autocomplete';
import { foldGutter, indentOnInput, syntaxHighlighting, defaultHighlightStyle, bracketMatching, indentUnit, foldKeymap } from '@codemirror/language';
import { lintKeymap } from '@codemirror/lint';
import { materialLight, materialDark } from '@uiw/codemirror-theme-material';
import { PostgreSQL, sql } from '@codemirror/lang-sql';
import { javascript } from '@codemirror/lang-javascript';
import { Button, Select, SelectItem } from '@nextui-org/react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { usePreferences } from '@/components/PreferencesProvider';
import { getCustomQueryStateFromURLParams, getURLParamsFromCustomQueryState } from '@/components/adminer/URLParamsState';
import { api } from '@/api';
import { AVAILABLE_VIEWS } from '@/components/adminer/Views';
import { ExportComponent } from '@/components/adminer/ExportComponent';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { View } from '@/types/adminer/View';
import { DatasourceType, type Datasource } from '@/types/datasource/Datasource';
import type { DataResponse, ErrorResponse } from '@/types/adminer/DataResponse';

const EXAMPLE_QUERY: Record<DatasourceType, string> = {
    [DatasourceType.neo4j]: 'MATCH (u)-[friend:FRIEND]->(f) WHERE f.id = \'user_005\' RETURN u, friend, f;',
    [DatasourceType.mongodb]: '{"find": "business", "filter": {"attributes.wifi": "free"}}',
    [DatasourceType.postgresql]: 'SELECT u.name, u.fans, c.business_id, c.date, c.text, c.stars FROM "user" u JOIN "comment" c ON (u.user_id = c.user_id) WHERE c.stars >= 2;',
    [DatasourceType.csv]: '',
    [DatasourceType.json]: '',
    [DatasourceType.jsonld]: '',
};

/**
 * @param datasource The selected datasource
 * @param datasources All active datasources
 */
type AdminerCustomQueryPageProps = Readonly<{
    datasource: Datasource;
    datasources: Datasource[];
}>;

/**
 * Component for fetching the data using custom query
 */
export function AdminerCustomQueryPage({ datasource, datasources }: AdminerCustomQueryPageProps) {
    const { theme } = usePreferences().preferences;
    const [ view, setView ] = useState(View.table);
    const [ queryResult, setQueryResult ] = useState<DataResponse | ErrorResponse>();
    const [ searchParams ] = useSearchParams();
    const [ query, setQuery ] = useState(() => {
        const { query: newQuery } = getCustomQueryStateFromURLParams(searchParams);
        return newQuery ?? '';
    });
    const queryRef = useRef(query);

    // Allows to rerender the component with no need to change the extensions with each render
    const onQueryChange = useCallback((newQuery: string) => {
        queryRef.current = newQuery;
        setQuery(newQuery);
    }, []);

    useEffect(() => {
        setQueryResult(undefined);
        onQueryChange('');
    }, [ datasource ]);

    // Sync state with URL search parameters
    useEffect(() => {
        const { query: newQuery } = getCustomQueryStateFromURLParams(searchParams);
        if (newQuery !== undefined && newQuery !== query)
            onQueryChange(newQuery);
    }, [ searchParams ]);

    const execute = useCallback(async () => {
        const query = queryRef.current;

        window.history.pushState({}, '', '?' + getURLParamsFromCustomQueryState(query, datasource));

        const queryResult = await api.adminer.getQueryResult({ datasourceId: datasource.id }, { query });

        if (queryResult.status) {
            setQueryResult(queryResult.data);
        }
        else {
            setQueryResult({
                message: queryResult.error?.data ? String(queryResult.error.data) : 'Failed to fetch query result',
            });
        }
    }, [ datasource ]);

    const extensions = useMemo(() => {
        const customKeymap = [ {
            key: 'Mod-Enter',
            run: () => {
                void execute();
                return true;
            },
        } ];

        return createExtensions(datasource.type, customKeymap);
    }, [ datasource.type, execute ]);

    return (
        <>
            <div className='mt-1'>
                <CodeMirror
                    value={query}
                    onChange={onQueryChange}
                    extensions={extensions}
                    basicSetup={false}
                    theme={theme === 'light' ? materialLight : materialDark}
                    minHeight='105.97px'
                />

                <Button
                    className='mt-1 items-center gap-1 min-w-40'
                    size='sm'
                    color='primary'
                    onPress={execute}
                >
                    EXECUTE QUERY
                </Button>

                <Button
                    className='ml-2 mt-1 items-center gap-1 min-w-40'
                    size='sm'
                    onPress={() => onQueryChange(EXAMPLE_QUERY[datasource.type])}
                >
                    SHOW QUERY EXAMPLE
                </Button>

                {AVAILABLE_VIEWS[datasource.type].length > 1 && (
                    <Select
                        items={AVAILABLE_VIEWS[datasource.type].entries()}
                        label='View'
                        labelPlacement='outside-left'
                        classNames={
                            { label:'sr-only' }
                        }
                        size='sm'
                        placeholder='Select view'
                        className='ml-1 max-w-xs align-middle'
                        selectedKeys={[ view ]}
                    >
                        {AVAILABLE_VIEWS[datasource.type].map(v => (
                            <SelectItem
                                key={v}
                                onPress={() => setView(v)}
                            >
                                {v}
                            </SelectItem>
                        ))}
                    </Select>
                )}

                {queryResult && 'data' in queryResult && (
                    <span className='ml-2'>
                        <ExportComponent data={queryResult}/>
                    </span>
                )}
            </div>

            <div className='flex grow min-h-0 mt-2'>
                {queryResult && 'message' in queryResult && (<>
                    {queryResult.message}
                </>)}

                {queryResult && 'data' in queryResult && (
                    <DatabaseView view={AVAILABLE_VIEWS[datasource.type].length === 1 ? AVAILABLE_VIEWS[datasource.type][0] : view} data={queryResult} kindReferences={[]} kindName={''} datasourceId={datasource.id} datasources={datasources} />
                )}

            </div>
        </>
    );
}

function createExtensions(datasourceType: DatasourceType | undefined, customKeymap: KeyBinding[] = []): Extension[] {
    const keymaps: KeyBinding[] = [
        ...customKeymap,
        ...closeBracketsKeymap,
        ...defaultKeymap,
        ...searchKeymap,
        ...historyKeymap,
        ...foldKeymap,
        ...completionKeymap,
        ...lintKeymap,
    ];

    return [
        lineNumbers(),
        highlightActiveLineGutter(),
        highlightSpecialChars(),
        history(),
        foldGutter(),
        drawSelection(),
        dropCursor(),
        EditorState.allowMultipleSelections.of(true),
        indentOnInput(),
        syntaxHighlighting(defaultHighlightStyle, { fallback: true }),
        bracketMatching(),
        closeBrackets(),
        autocompletion(),
        rectangularSelection(),
        crosshairCursor(),
        highlightActiveLine(),
        highlightSelectionMatches(),
        indentUnit.of('    '),
        getLanguageExtension(datasourceType),
        keymap.of(keymaps.flat()),
    ] satisfies Extension[];
}

function getLanguageExtension(datasourceType: DatasourceType | undefined) {
    switch (datasourceType) {
    case DatasourceType.postgresql:
        return sql({ dialect: PostgreSQL });
    case DatasourceType.mongodb:
        return javascript();
    default:
        return sql();
    }
}
