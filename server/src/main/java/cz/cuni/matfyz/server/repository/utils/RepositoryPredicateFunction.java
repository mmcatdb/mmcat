package cz.cuni.matfyz.server.repository.utils;

/**
 * @author jachym.bartik
 */
public interface RepositoryPredicateFunction<I, F> {

    boolean execute(I input, F object);

}