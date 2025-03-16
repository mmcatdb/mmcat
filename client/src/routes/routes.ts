class ParametrizedRoute<TParam extends string = never, TQuery extends string = never> {
    constructor(
        readonly path: string,
        readonly id: string,
    ) {}

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

const categoryIndex = new ParametrizedRoute<'categoryId'>('/schema-categories/:categoryId', 'category');

export const routes = {
    home: new ParametrizedRoute('/', 'home'),
    categories: '/schema-categories',
    about: '/about',
    datasources: '/datasources',
    adminer: '/adminer',
    category: {
        index: categoryIndex,
        editor: categoryIndex.child('/editor', 'editor'),
        datasources: categoryIndex.child('/datasources', 'datasources'),
        // mapping: categoryIndex.child<'categoryId' | 'mappingId'>('/mappings/:mappingId', 'mappings'),
        mapping: categoryIndex.child<'categoryId' | 'datasourceId' | 'mappingId'>('datasources/:datasourceId/mappings/:mappingId', 'mapping'),
        querying: categoryIndex.child('/querying', 'querying'),
        actions: categoryIndex.child('/actions', 'actions'),
        jobs: categoryIndex.child('/jobs', 'jobs'),
        job: categoryIndex.child<'categoryId' | 'jobId'>('/jobs/:jobId', 'job'),
    },
} as const;
