package cz.matfyz.core.metadata;

public class MetadataObjex {

    public final String label;
    public final Position position;

    public MetadataObjex(String label, Position position) {
        this.label = label;
        this.position = position;
    }

    public record Position(
        double x,
        double y
    ) {

        public static Position createDefault() {
            return new Position(0, 0);
        }

    }

}
