package cz.matfyz.querying.parsing;

class MorphismNode {

    public final String name;
    public final ValueNode valueNode;

    public MorphismNode(String morphism, ValueNode valueNode) {
        this.name = morphism;
        this.valueNode = valueNode;
    }

}