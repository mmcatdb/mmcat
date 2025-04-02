package cz.matfyz.evolution.category;

import cz.matfyz.evolution.category.complex.*;

public interface SchemaEvolutionVisitor<T> {

    T visit(Composite operation);
    T visit(CreateMorphism operation);
    T visit(CreateObjex operation);
    T visit(DeleteMorphism operation);
    T visit(DeleteObjex operation);
    T visit(UpdateMorphism operation);
    T visit(UpdateObjex operation);

    // Complex
    T visit(Copy operation);
    T visit(Move operation);
    T visit(Group operation);
    T visit(UnGroup operation);
    T visit(Transform operation);

}
