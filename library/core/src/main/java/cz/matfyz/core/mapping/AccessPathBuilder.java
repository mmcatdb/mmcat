package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.Accessor;
import cz.matfyz.core.mapping.Name.IndexName;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.mapping.Name.TypedName;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class AccessPathBuilder {

    public ComplexProperty root(AccessPath... subpaths) {
        return new ComplexProperty(new TypedName(TypedName.ROOT), Signature.empty(), List.of(subpaths));
    }

    // Simple property

    public SimpleProperty simple(String name, Accessor<Signature> signature) {
        return new SimpleProperty(new StringName(name), signature.access());
    }

    public SimpleProperty simple(Name name, Accessor<Signature> signature) {
        return new SimpleProperty(name, signature.access());
    }

    // Complex property

    public ComplexProperty complex(String name, Accessor<Signature> signature, AccessPath... subpaths) {
        return new ComplexProperty(new StringName(name), signature.access(), List.of(subpaths));
    }

    public ComplexProperty complex(Name name, Accessor<Signature> signature, AccessPath... subpaths) {
        return new ComplexProperty(name, signature.access(), List.of(subpaths));
    }

    // Both types

    /** If <code>subpaths</code> is empty, returns {@link SimpleProperty}. Otherwise returns {@link ComplexProperty}. */
    public AccessPath property(Name name, Accessor<Signature> signature, AccessPath... subpaths) {
        return subpaths.length == 0
            ? simple(name, signature)
            : complex(name, signature, subpaths);
    }

    // Mental illnesses

    public AccessPath auxiliary(String name, AccessPath... subpaths) {
        return property(new StringName(name), Signature.empty(), subpaths);
    }

    /** A key-value map. If <code>subpaths</code> is empty, corresponds to a "simple" map. Otherwise it's a "complex" map. */
    public ComplexProperty dynamic(Accessor<Signature> toMap, @Nullable String pattern, Accessor<Signature> toKey, Accessor<Signature> toValue, AccessPath... subpaths) {
        return complex(new DynamicName(pattern), toMap,
            simple(new TypedName(TypedName.KEY), toKey),
            property(new TypedName(TypedName.VALUE), toValue, subpaths)
        );
    }

    /** A single-dimensional array. If <code>subpaths</code> is empty, corresponds to a "simple" array property. Otherwise it's a "complex" array. */
    public ComplexProperty indexed(String name, Accessor<Signature> toArray, Accessor<Signature> toIndex, Accessor<Signature> toValue, AccessPath... subpaths) {
        return complex(name, toArray,
            simple(new IndexName(0), toIndex),
            property(new TypedName(TypedName.VALUE), toValue, subpaths)
        );
    }

    /** A multi-dimensional array. If <code>subpaths</code> is empty, corresponds to a "simple" array property. Otherwise it's a "complex" array. */
    public ComplexProperty indexed(String name, Accessor<Signature> toArray, Iterable<Accessor<Signature>> toIndexes, Accessor<Signature> toValue, AccessPath... subpaths) {
        final var innerSubpaths = new ArrayList<AccessPath>();

        for (final var toIndex : toIndexes)
            innerSubpaths.add(simple(new IndexName(innerSubpaths.size()), toIndex));

        innerSubpaths.add(property(new TypedName(TypedName.VALUE), toValue, subpaths));

        return complex(name, toArray, innerSubpaths.toArray(AccessPath[]::new));
    }

}
