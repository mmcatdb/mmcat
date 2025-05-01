package cz.matfyz.abstractwrappers.querycontent;

import java.util.List;

import cz.matfyz.core.adminer.AdminerFilter;

public class KindNameFilterQuery implements QueryContent {

    public final KindNameQuery kindNameQuery;

    private final List<AdminerFilter> filters;

    public List<AdminerFilter> getFilters() {
        return filters;
    }

    public KindNameFilterQuery(KindNameQuery kindNameQuery, List<AdminerFilter> filters) {
        this.kindNameQuery = kindNameQuery;
        this.filters = filters;
    }

    @Override public String toString() {
        return kindNameQuery.toString() + "\nfilters: " + filters;
    }

}
