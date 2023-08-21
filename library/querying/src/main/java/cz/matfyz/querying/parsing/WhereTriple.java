package cz.matfyz.querying.parsing;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.exception.ParsingException;

import java.util.Arrays;
import java.util.List;

public class WhereTriple implements Statement {

    public final Variable subject;
    public final BaseSignature signature;
    public final ValueNode object;
    
    public WhereTriple(Variable subject, BaseSignature signature, ValueNode object) {
        this.subject = subject;
        this.signature = signature;
        this.object = object;
    }

    static List<WhereTriple> fromCommonTriple(CommonTriple common) {
        try {
            final var bases = Arrays.stream(common.predicate.split("/"))
                .map(base -> Signature.createBase(Integer.parseInt(base)))
                .toList();

            return createSplit(common.subject, bases, common.object);
        }
        catch (NumberFormatException e) {
            throw ParsingException.signature(common.predicate);
        }
    }

    /**
     * For each compound morphism (A) -x/y-> (B), split it by inserting intermediate internal variables in such a way that each triple contains a base morphism only.
     */
    private static List<WhereTriple> createSplit(Variable subject, List<BaseSignature> bases, ValueNode object) {
        var splitTriples = bases.stream().map(base -> {
            var editableTriple = new EditableWhereTriple();
            editableTriple.signature = base;
            return editableTriple;
        }).toList();
            
        splitTriples.get(0).subject = subject;
        splitTriples.get(splitTriples.size() - 1).object = object;

        for (int i = 0; i < splitTriples.size() - 1; i++) {
            Variable newVariable = Variable.generated();
            splitTriples.get(i).object = newVariable;
            splitTriples.get(i + 1).subject = newVariable;
        }

        return splitTriples.stream().map(EditableWhereTriple::toTriple).toList();
    }

    private static class EditableWhereTriple {
        Variable subject;
        BaseSignature signature;
        ValueNode object;

        /**
         * For each triple with a base dual morphism, reverse its direction so that we have a non-dual morphism.
         */
        WhereTriple toTriple() {
            if (!signature.isDual())
                return new WhereTriple(subject, signature, object);

            if (!(object instanceof Variable variable))
                // TODO - Is this necessary? Shouldn't the where triples always had Variable as object?
                throw GeneralException.message("WTF type inconsistency in reverseBaseMorphisms");

            return new WhereTriple(variable, signature.dual(), subject);
        }
    }

    @Override
    public String toString() {
        return subject.toString() + " " + signature.toString() + " " + object.toString();
    }

}