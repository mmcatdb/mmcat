package cz.cuni.matfyz.core.mapping;

/**
 * This class represents a kind in a database. It's identified by the databaseId and mapping (more precisely, by the name of the collection, i.e., kindName).
 * Multiple instances of the same kind can be used at the same time and they are all unique (each is identified by the internal id).
 */
public class KindInstance implements Comparable<KindInstance> {

    public final Mapping mapping;
    public final String databaseId;
    private final int id;

    private KindInstance(Mapping mapping, String databaseId, int id) {
        this.mapping = mapping;
        this.databaseId = databaseId;
        this.id = id;
    }

    @Override
    public int compareTo(KindInstance other) {
        return id - other.id;
    }

    public static class KindInstanceBuilder {

        private int id = 0;

        public KindInstance next(Mapping mapping, String databaseId) {
            return new KindInstance(mapping, databaseId, id++);
        }

    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append("{\n")
            .append("    id: ").append(id).append(",\n")
            .append("    kindName: ").append(databaseId).append(",\n")
            .append("    databaseId: ").append(mapping.kindName()).append(",\n")
            .append("}");

        return builder.toString();
    }
    
}
