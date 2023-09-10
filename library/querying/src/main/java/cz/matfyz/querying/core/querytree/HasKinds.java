package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.database.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface HasKinds {
    
    Set<Kind> kinds();

    public static record SplitResult<T extends HasKinds>(List<T> included, List<T> rest) {}

    public static <T extends HasKinds> SplitResult<T> splitByKinds(List<T> all, Set<Kind> kinds) {
        final var included = new ArrayList<T>();
        final var rest = new ArrayList<T>();
        all.forEach(item -> (kinds.containsAll(item.kinds()) ? included : rest).add(item));

        return new SplitResult<>(included, rest);
    }

}
