package cz.matfyz.inference.edit;

/**
 * The {@code PatternSegment} class represents a segment of a pattern in an inference edit.
 * It consists of a node name and a direction, which indicates the relationship or transition
 * between nodes in the pattern.
 *
 * <p>The direction can have specific values: "", "->", "<-", "@->", or "@<-", where the "@" symbol
 * represents a morphism on itself.
 *
 * @param nodeName The name of the node in the pattern.
 * @param direction The direction of the transition from this node.
 *                  It can be "", "->", "<-", "@->", or "@<-".
 */
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
