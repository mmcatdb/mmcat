package cz.cuni.matfyz.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachymb.bartik
 */
public abstract class Common {

    private Common() {}
    
    /**
     * A common method for all enums since they can't have another base class.
     * @param <T> type of the enum
     * @param enumClass class of the enum
     * @param string value we want to convert to the enum
     * @return corresponding enum (or null)
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> enumClass, String string) {
        try {
            return Enum.valueOf(enumClass, string);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static class Tree<K extends Comparable<K>> {
        public final Map<K, Tree<K>> value = new TreeMap<>();
    }

    /**
     * Both nodes and leaves are tree maps.
     * We assume there is no list that's an subset of other list. I.e., if there would be list [ a, b ], there can't be [ a ]. There can be [ a, c ] or another [ a, b ].
     */
    public static <K extends Comparable<K>> Tree<K> getTreeFromLists(Collection<List<K>> lists) {
        final var output = new Tree<K>();

        for (final Collection<K> list : lists) {
            var context = output;
            for (final K item : list)
                context = context.value.computeIfAbsent(item, i -> new Tree<K>());
        }

        return output;
    }

}
