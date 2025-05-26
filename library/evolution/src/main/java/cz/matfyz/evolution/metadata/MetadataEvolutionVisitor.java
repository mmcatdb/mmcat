package cz.matfyz.evolution.metadata;

public interface MetadataEvolutionVisitor<T> {

    T visit(ObjexMetadata operation);
    T visit(MorphismMetadata operation);

}
