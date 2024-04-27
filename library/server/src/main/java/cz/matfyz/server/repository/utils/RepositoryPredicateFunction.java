package cz.matfyz.server.repository.utils;

public interface RepositoryPredicateFunction<I, F> {

    boolean execute(I input, F object);

}
