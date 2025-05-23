package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.mapping.Name.TypedName;

import java.util.List;

public class AccessPathBuilder {

    // Complex property

    public ComplexProperty complex(String name, Signature signature, AccessPath... subpaths) {
        return new ComplexProperty(new StringName(name), signature, List.of(subpaths));
    }

    public ComplexProperty complex(String name, BuilderMorphism morphism, AccessPath... subpaths) {
        return complex(name, morphism.signature(), subpaths);
    }

    public ComplexProperty complex(Signature name, boolean isKey, Signature signature, AccessPath... subpaths) {
        final var type = isKey ? TypedName.KEY : TypedName.INDEX;
        return new ComplexProperty(new DynamicName(type, name, null), signature, List.of(subpaths));
    }

    // No need for type here because pattern does make sense only for keys.
    public ComplexProperty complex(Signature name, String pattern, Signature signature, AccessPath... subpaths) {
        return new ComplexProperty(new DynamicName(TypedName.KEY, name, pattern), signature, List.of(subpaths));
    }

    public ComplexProperty auxiliary(String name, AccessPath... subpaths) {
        return new ComplexProperty(new StringName(name), Signature.createEmpty(), List.of(subpaths));
    }

    public ComplexProperty auxiliary(Signature name, boolean isKey, AccessPath... subpaths) {
        final var type = isKey ? TypedName.KEY : TypedName.INDEX;
        return new ComplexProperty(new DynamicName(type, name, null), Signature.createEmpty(), List.of(subpaths));
    }

    // No need for type here because pattern does make sense only for keys.
    public ComplexProperty auxiliary(Signature name, String pattern, AccessPath... subpaths) {
        return new ComplexProperty(new DynamicName(TypedName.KEY, name, pattern), Signature.createEmpty(), List.of(subpaths));
    }

    public ComplexProperty root(AccessPath... subpaths) {
        return new ComplexProperty(new TypedName(TypedName.ROOT), Signature.createEmpty(), List.of(subpaths));
    }

    // Simple property

    public SimpleProperty simple(String name, Signature signature) {
        return new SimpleProperty(new StringName(name), signature);
    }

    public SimpleProperty simple(String name, BuilderMorphism morphism) {
        return simple(name, morphism.signature());
    }

    public SimpleProperty simple(Signature name, boolean isKey, Signature signature) {
        final var type = isKey ? TypedName.KEY : TypedName.INDEX;
        return new SimpleProperty(new DynamicName(type, name, null), signature);
    }

    // No need for type here because pattern does make sense only for keys.
    public SimpleProperty simple(Signature name, String pattern, Signature signature) {
        return new SimpleProperty(new DynamicName(TypedName.KEY, name, pattern), signature);
    }

}
