package cz.matfyz.core.mapping;

/**
 * This class represents a kind in a database. It's identified by the databaseId and mapping (more precisely, by the name of the collection, i.e., kindName).
 * Multiple instances of the same kind can be used at the same time and they are all unique (each is identified by the internal id).
 */
public class Kind implements Comparable<Kind> {

    public final Mapping mapping;
    public final String databaseId;
    private final int id;

    private Kind(Mapping mapping, String databaseId, int id) {
        this.mapping = mapping;
        this.databaseId = databaseId;
        this.id = id;
    }

    @Override
    public int compareTo(Kind other) {
        return id - other.id;
    }

    /**
     * When joining in databases, each kind can be accessed only once. Therefore, we have to identify the kinds by their database and name.
     */
    public boolean isSameKind(Kind other) {
        return databaseId.equals(other.databaseId) && mapping.kindName().equals(other.mapping.kindName());
    }

    public static class KindBuilder {

        private int id = 0;

        public Kind next(Mapping mapping, String databaseId) {
            return new Kind(mapping, databaseId, id++);
        }

    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append("{\n")
            .append("    id: ").append(id).append(",\n")
            .append("    databaseId: ").append(databaseId).append(",\n")
            .append("    kindName: ").append(mapping.kindName()).append(",\n")
            .append("}");

        return builder.toString();
    }
    
}
