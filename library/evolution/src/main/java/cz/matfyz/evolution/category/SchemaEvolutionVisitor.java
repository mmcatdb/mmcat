package cz.matfyz.evolution.category;

public interface SchemaEvolutionVisitor<T> {

    T visit(Composite operation);
    T visit(CopyObjex operation);
    T visit(CreateMorphism operation);
    T visit(CreateObjex operation);
    T visit(DeleteMorphism operation);
    T visit(DeleteObjex operation);
    T visit(GroupProperties operation);
    T visit(JoinProperties operation);
    T visit(MoveProperty operation);
    T visit(SplitProperty operation);
    T visit(UngroupProperty operation);
    T visit(UpdateMorphism operation);
    T visit(UpdateObjex operation);

}
