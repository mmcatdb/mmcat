package cz.matfyz.core.identifiers;

import java.util.List;

/**
 * This class represents a base signature of a morphism or a dual to a morphism.
 */
public class BaseSignature extends Signature {

    private final int id;

    public BaseSignature(int id) {
        super(new int[] { id });
        this.id = id;
    }

    @Override public List<BaseSignature> toBases() {
        return List.of(this);
    }

    @Override public Signature cutLast() {
        return Signature.createEmpty();
    }

    @Override public BaseSignature getLast() {
        return this;
    }

    @Override public Signature cutFirst() {
        return Signature.createEmpty();
    }

    @Override public BaseSignature getFirst() {
        return this;
    }

    @Override public BaseSignature dual() {
        return createBase(-id);
    }

    public boolean isDual() {
        return id < 0;
    }

    /** In other words, "to non dual". */
    public BaseSignature toAbsolute() {
        return isDual() ? dual() : this;
    }

    @Override public String toString() {
        return Integer.toString(id);
    }

    @Override public boolean equals(Object object) {
        return object instanceof BaseSignature signature && compareTo(signature) == 0;
    }

}
