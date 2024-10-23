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

const projectIndex = new ParametrizedRoute<'projectId'>('/projects/:projectId', 'project');

export const routes = {
    home: new ParametrizedRoute('/', 'home'),
    about: 'about',
    project: {
        index: projectIndex,
        databases: projectIndex.child('/databases', 'databases'),
        models: projectIndex.child('/models', 'models'),
        querying: projectIndex.child('/querying', 'querying'),
    },
    datasources: 'datasources',
    adminer: 'adminer',
    categories: 'schema-categories',
} as const;
