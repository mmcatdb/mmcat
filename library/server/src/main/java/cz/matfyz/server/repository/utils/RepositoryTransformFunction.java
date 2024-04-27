package cz.matfyz.server.repository.utils;

public interface RepositoryTransformFunction<T, I, F> {

    T execute(I input, F object);

}
