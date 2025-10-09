package cz.matfyz.core.identifiers;

import java.util.List;

/**
 * This class represents a base signature of a morphism or a dual to a morphism.
 */
public class BaseSignature extends Signature {

    public BaseSignature(int id) {
        super(new int[] { id });
    }

    @Override public List<BaseSignature> toBases() {
        return List.of(this);
    }

    @Override public Signature cutLast() {
        return Signature.empty();
    }

    @Override public BaseSignature getLast() {
        return this;
    }

    @Override public Signature cutFirst() {
        return Signature.empty();
    }

    @Override public BaseSignature getFirst() {
        return this;
    }

    @Override public BaseSignature dual() {
        return createBase(-ids[0]);
    }

    public boolean isDual() {
        return ids[0] < 0;
    }

    /** In other words, "to non dual". */
    public BaseSignature toAbsolute() {
        return isDual() ? dual() : this;
    }

    @Override public String toString() {
        return Integer.toString(ids[0]);
    }

    @Override public boolean equals(Object object) {
        return object instanceof BaseSignature base && compareTo(base) == 0;
    }

    @Override public int compareTo(Signature signature) {
        return signature instanceof BaseSignature base
            ? compareTo(base)
            : 1 - signature.ids.length;
    }

    public int compareTo(BaseSignature signature) {
        return ids[0] - signature.ids[0];
    }

}
