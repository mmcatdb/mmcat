package cz.matfyz.querying.core.querytree;

public class OptionalNode extends OperationNode {
 
    @Override
    public Operation operation() {
        return Operation.Optional;
    }

    public OptionalNode(GroupNode group) {
        super(group);
    }

    @Override
    public OptionalNode updateGroup(GroupNode group) {
        return new OptionalNode(group);
    }

}
