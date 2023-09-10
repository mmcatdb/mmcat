package cz.matfyz.querying.core.querytree;

public class UnionNode extends OperationNode {
    
    @Override
    public Operation operation() {
        return Operation.Union;
    }

    public UnionNode(GroupNode group) {
        super(group);
    }

    @Override
    public UnionNode updateGroup(GroupNode group) {
        return new UnionNode(group);
    }

}
