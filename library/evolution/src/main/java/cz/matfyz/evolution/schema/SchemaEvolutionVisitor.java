package cz.matfyz.evolution.schema;

public interface SchemaEvolutionVisitor<T> {

    T visit(Composite operation);
    T visit(CreateMorphism operation);
    T visit(CreateObject operation);
    T visit(DeleteMorphism operation);
    T visit(DeleteObject operation);
    T visit(UpdateMorphism operation);
    T visit(UpdateObject operation);

}
