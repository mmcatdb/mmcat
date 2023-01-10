package cz.cuni.matfyz.server.repository.utils;

/**
 * @author jachym.bartik
 */
public interface RepositoryTransformFunction<T, I, F> {

    T execute(I input, F object);

}
