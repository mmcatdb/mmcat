package cz.matfyz.core.utils.printable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Enables overriding the toString method of specific classes when printed via {@link Printer#append(Object)}.
 */
public class Stringifier {

    private Map<Class<?>, Function<?, String>> overrides = new HashMap<>();

    public <T> Stringifier set(Class<T> clazz, Function<T, String> override) {
        overrides.put(clazz, override);
        return this;
    }

    public <T> Stringifier unset(Class<?> clazz) {
        overrides.remove(clazz);
        return this;
    }

    @SuppressWarnings("unchecked")
    String apply(Object object) {
        final var override = overrides.get(object.getClass());
        if (override == null)
            return object.toString();

        return ((Function<Object, String>) override).apply(object);
    }

}
