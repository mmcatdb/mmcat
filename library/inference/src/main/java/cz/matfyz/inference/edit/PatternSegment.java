package cz.matfyz.inference.edit;

public record PatternSegment(
    String nodeName,
    /** These are either: "", "->", "<-", "@->" or "@<-" (where the @ symbol represents a morphism on itself) */
    String direction
) {

    @Override
    public String toString() {
        return nodeName + " " + direction;
    }

}
