package cz.matfyz.evolution.metadata;

public interface MetadataEvolutionVisitor<T> {

    T visit(ObjectMetadata operation);
    T visit(MorphismMetadata operation);

}
