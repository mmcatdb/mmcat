package cz.matfyz.evolution.schema;

public interface SchemaEvolutionVisitor<T> {

    T visit(Composite operation);
    T visit(CreateMorphism operation);
    T visit(CreateObject operation);
    T visit(DeleteMorphism operation);
    T visit(DeleteObject operation);
    T visit(EditMorphism operation);
    T visit(EditObject operation);

}
