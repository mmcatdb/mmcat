/**
 * Represents a URL route that can be parameterized with dynamic path and query values.
 */
class ParametrizedRoute<TParam extends string = never, TQuery extends string = never> {
    constructor(
        readonly path: string,
        readonly id: string,
    ) {}

    /**
     * Resolves the full URL string by replacing dynamic parameters and appending query strings.
     */
    resolve(params: Record<TParam, string>, query?: Record<TQuery, string>): string {
        let value = this.path;
        for (const key of Object.keys(params))
            value = value.replace(':' + key, params[key as keyof typeof params]);

        if (query) {
            const queryString = Object.entries(query).map(([ key, value ]) => `${key}=${value}`).join('&');
            if (queryString.length > 0)
                value += '?' + queryString;
        }

        return value;
    }

    child<TChildParam extends string = never, TChildQuery extends string = never>(childPath: string, childId: string) {
        return new ParametrizedRoute<TParam | TChildParam, TChildQuery>(this.path + childPath, `${this.id}-${childId}`);
    }
}

/**
 * Base route for schema category related paths.
 */
const categoryIndex = new ParametrizedRoute<'categoryId'>('/schema-categories/:categoryId', 'category');

/**
 * Base route for datasources related paths.
 */
const datasourcesIndex = new ParametrizedRoute('/datasources', 'datasources');

export const routes = {
    home: new ParametrizedRoute('/', 'home'),
    // Static routes
    about: '/about',
    adminer: '/adminer',
    categories: '/schema-categories',
    // Nested and dynamic routes
    category: {
        index: categoryIndex,
        editor: categoryIndex.child('/editor', 'editor'),
        datasources: {
            list: categoryIndex.child('/datasources', 'datasources'),
            detail: categoryIndex.child<'categoryId' | 'datasourceId'>('/datasources/:datasourceId', 'datasource'),
            newMapping: categoryIndex.child<'categoryId' | 'datasourceId'>('/datasources/:datasourceId/new-mapping', 'new-mapping'),
        },
        mapping: categoryIndex.child<'categoryId' | 'mappingId'>('/mappings/:mappingId', 'mappings'),
        queries: {
            list: categoryIndex.child('/queries', 'queries'),
            detail: categoryIndex.child<'categoryId' | 'queryId'>('/queries/:queryId', 'query'),
            new: categoryIndex.child('/queries/new', 'new-query'),
        },
        actions: {
            list: categoryIndex.child('/actions', 'actions'),
            detail: categoryIndex.child<'categoryId' | 'actionId'>('/actions/:actionId', 'action'),
            new: categoryIndex.child('/actions/new', 'new-action'),
        },
        jobs: categoryIndex.child('/jobs', 'jobs'),
        job: categoryIndex.child<'categoryId' | 'jobId'>('/jobs/:jobId', 'job'),
        files: {
            list: categoryIndex.child('/files', 'files'),
            detail: categoryIndex.child<'categoryId' | 'fileId'>('/files/:fileId', 'file'),
        },
        adaptation: categoryIndex.child('/adaptation', 'adaptation'),
    },
    datasources: {
        list: datasourcesIndex,
        detail: datasourcesIndex.child<'datasourceId'>('/:datasourceId', 'datasource'),
    },
    dev: '/dev',
} as const;
