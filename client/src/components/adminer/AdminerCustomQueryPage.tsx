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
import { getCustomQueryStateFromURLParams, getURLParamsFromCustomQueryState } from '@/components/adminer/URLParamsState';
import { api } from '@/api';
import { EXAMPLE_QUERY } from '@/components/adminer/Queries';
import { ExportComponent } from '@/components/adminer/ExportComponent';
import { View } from '@/types/adminer/View';
import { DatasourceType, type Datasource } from '@/types/datasource/Datasource';
import type { DataResponse, ErrorResponse } from '@/types/adminer/DataResponse';
import type { Theme } from '@/components/PreferencesProvider';
import { CustomQueryDatabaseView } from './CustomQueryDatabaseView';

type AdminerCustomQueryPageProps = Readonly<{
    datasource: Datasource;
    datasources: Datasource[];
    theme: Theme;
}>;

export function AdminerCustomQueryPage({ datasource, datasources, theme }: AdminerCustomQueryPageProps) {
    const availableViews = [ View.table, View.document ];
    const [ view, setView ] = useState<View>(View.table);
    const [ queryResult, setQueryResult ] = useState<DataResponse | ErrorResponse>();
    const [ searchParams ] = useSearchParams();
    const [ query, setQuery ] = useState<string>(() => {
        const { query: newQuery } = getCustomQueryStateFromURLParams(searchParams);
        return newQuery ?? '';
    });
    const queryRef = useRef(query);

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
                aria-label='Execute query'
                type='submit'
                color='primary'
                onPress={execute}
            >
                EXECUTE QUERY
            </Button>

            <Button
                className='ml-2 mt-1 items-center gap-1 min-w-40'
                size='sm'
                aria-label='Show query example'
                type='submit'
                onPress={() => onQueryChange(EXAMPLE_QUERY[datasource.type])}
            >
                SHOW QUERY EXAMPLE
            </Button>

            {datasource.type === DatasourceType.neo4j && (
                <Select
                    items={availableViews.entries()}
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
                    {availableViews.map(v => (
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

            <div className='mt-1'>
                {queryResult && 'message' in queryResult && (<>
                    {queryResult.message}
                </>)}

                {queryResult && 'data' in queryResult && (
                    <CustomQueryDatabaseView queryResult={queryResult} datasource={datasource} datasources={datasources} view={view} />
                )}

            </div>
        </div>
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
