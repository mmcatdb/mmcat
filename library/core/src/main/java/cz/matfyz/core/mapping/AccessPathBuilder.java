package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;

import java.util.List;

public class AccessPathBuilder {

    // Complex property

    public ComplexProperty complex(String name, Signature signature, AccessPath... subpaths) {
        return new ComplexProperty(new StaticName(name), signature, List.of(subpaths));
    }

    public ComplexProperty complex(String name, BuilderMorphism morphism, AccessPath... subpaths) {
        return complex(name, morphism.signature(), subpaths);
    }

    public ComplexProperty complex(Signature name, Signature signature, AccessPath... subpaths) {
        return new ComplexProperty(new DynamicName(name, null), signature, List.of(subpaths));
    }

    public ComplexProperty complex(Signature name, String pattern, Signature signature, AccessPath... subpaths) {
        return new ComplexProperty(new DynamicName(name, pattern), signature, List.of(subpaths));
    }

    public ComplexProperty complex(BuilderMorphism name, BuilderMorphism morphism, AccessPath... subpaths) {
        return complex(name.signature(), morphism.signature(), subpaths);
    }

    public ComplexProperty auxiliary(String name, AccessPath... subpaths) {
        return new ComplexProperty(new StaticName(name), Signature.createEmpty(), List.of(subpaths));
    }

    public ComplexProperty auxiliary(Signature name, AccessPath... subpaths) {
        return new ComplexProperty(new DynamicName(name, null), Signature.createEmpty(), List.of(subpaths));
    }

    public ComplexProperty auxiliary(Signature name, String pattern, AccessPath... subpaths) {
        return new ComplexProperty(new DynamicName(name, pattern), Signature.createEmpty(), List.of(subpaths));
    }

    public ComplexProperty auxiliary(BuilderMorphism name, AccessPath... subpaths) {
        return auxiliary(name.signature(), subpaths);
    }

    public ComplexProperty root(AccessPath... subpaths) {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createEmpty(), List.of(subpaths));
    }

    // Simple property

    public SimpleProperty simple(String name, Signature signature) {
        return new SimpleProperty(new StaticName(name), signature);
    }

    public SimpleProperty simple(String name, BuilderMorphism morphism) {
        return simple(name, morphism.signature());
    }

    public SimpleProperty simple(Signature name, Signature signature) {
        return new SimpleProperty(new DynamicName(name, null), signature);
    }

    public SimpleProperty simple(Signature name, String pattern, Signature signature) {
        return new SimpleProperty(new DynamicName(name, pattern), signature);
    }

    public SimpleProperty simple(BuilderMorphism name, BuilderMorphism morphism) {
        return simple(name.signature(), morphism.signature());
    }

}
