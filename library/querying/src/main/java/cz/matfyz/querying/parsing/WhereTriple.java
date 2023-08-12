package cz.matfyz.querying.parsing;

import cz.matfyz.core.category.Signature;
import cz.matfyz.querying.exception.ParsingException;

import java.util.Arrays;
import java.util.List;

public class WhereTriple implements Statement {

    public final Variable subject;
    public final Signature signature;
    public final ValueNode object;
    
    public WhereTriple(Variable subject, Signature signature, ValueNode object) {
        this.subject = subject;
        this.signature = signature;
        this.object = object;
    }

    static WhereTriple fromCommonTriple(CommonTriple common) {
        try {
            var bases = Arrays.stream(common.predicate.split("/"))
                .map(base -> Signature.createBase(Integer.parseInt(base)))
                .toList();

            var signature = Signature.concatenate(bases);

            return new WhereTriple(common.subject, signature, common.object);
        }
        catch (NumberFormatException e) {
            throw ParsingException.signature(common.predicate);
        }
    }

    public List<WhereTriple> toBases() {
        var splitTriples = signature.toBases().stream().map(base -> {
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
        Signature signature;
        ValueNode object;

        WhereTriple toTriple() {
            return new WhereTriple(subject, signature, object);
        }
    }

    @Override
    public String toString() {
        return subject.toString() + " " + signature.toString() + " " + object.toString();
    }

}