package cz.matfyz.querying.algorithms;

import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryPreprocessor2 {

    private QueryPreprocessor2() {}

    public static List<WhereTriple> preprocessPattern(List<WhereTriple> pattern) {
        final var splitted = splitCompoundMorphisms(pattern);
        final var reversed = reverseBaseMorphisms(splitted);

        return reversed;
    }

    /**
     * For each compound morphism (A) -x/y-> (B), split it by inserting intermediate internal variables in such a way that each triple contains a base morphism only.
     */
    private static List<WhereTriple> splitCompoundMorphisms(List<WhereTriple> pattern) {
        final var output = new ArrayList<WhereTriple>();

        for (final var triple : pattern) {
            if (triple.signature.isBase())
                output.add(triple);
            else
                output.addAll(triple.toBases());
        }

        return output;
    }

    /**
     * For each triple with a base dual morphism, reverse its direction so that we have a non-dual morphism.
     */
    private static List<WhereTriple> reverseBaseMorphisms(List<WhereTriple> pattern) {
        final var output = new ArrayList<WhereTriple>();

        for (final var triple : pattern) {
            if (!triple.signature.isBaseDual()) {
                output.add(triple);
                continue;
            }
            
            if (!(triple.object instanceof Variable variable))
                // TODO - Is this necessary? Shouldn't the where triples always had Variable as object?
                throw GeneralException.message("WTF type inconsistency in reverseBaseMorphisms");

            output.add(new WhereTriple(variable, triple.signature.dual(), triple.subject));
        }

        return output;
    }

}