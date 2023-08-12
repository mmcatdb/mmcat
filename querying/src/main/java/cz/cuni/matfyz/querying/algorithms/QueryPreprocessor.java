package cz.cuni.matfyz.querying.algorithms;

import cz.cuni.matfyz.querying.exception.GeneralException;
import cz.cuni.matfyz.querying.parsing.Query;
import cz.cuni.matfyz.querying.parsing.Variable;
import cz.cuni.matfyz.querying.parsing.WhereClause;
import cz.cuni.matfyz.querying.parsing.WhereTriple;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryPreprocessor {

    private QueryPreprocessor() {}

    /**
     * Perform preprocessing on the query, according to the algorithm presented in the master's thesis.
     */
    public static Query preprocessQuery(Query query) {
        var whereTriples = splitCompoundMorphisms(query.where.triples);
        // The morphisms shouldn't be reversed. In the mappings, the morphisms are being found by their signature so the reversed one wouldn't be found.
        // But this is probably an issue of the query
        // whereTriples = reverseBaseMorphisms(whereTriples);

        var where = new WhereClause(
            whereTriples,
            query.where.variables,
            query.where.filters,
            query.where.values
        );

        return new Query(query.select, where, query.variables);
    }

    /**
     * For each compound morphism (A) -x/y-> (B), split it by inserting intermediate internal variables in such a way that each triple contains a base morphism only.
     */
    private static List<WhereTriple> splitCompoundMorphisms(List<WhereTriple> triples) {
        var transformedTriples = new ArrayList<WhereTriple>();

        for (var triple : triples) {
            if (triple.signature.isBase())
                transformedTriples.add(triple);
            else
                transformedTriples.addAll(triple.toBases());
        }

        return transformedTriples;
    }

    /**
     * For each triple with a base dual morphism, reverse its direction so that we have a non-dual morphism.
     */
    private static List<WhereTriple> reverseBaseMorphisms(List<WhereTriple> triples) {
        var transformedTriples = new ArrayList<WhereTriple>();

        for (var triple : triples) {
            if (!triple.signature.isBaseDual()) {
                transformedTriples.add(triple);
                continue;
            }
            
            if (!(triple.object instanceof Variable variable))
                // TODO - Is this necessary? Shouldn't the where triples always had Variable as object?
                throw GeneralException.message("WTF type inconsistency in reverseBaseMorphisms");

            transformedTriples.add(new WhereTriple(variable, triple.signature.dual(), triple.subject));
        }

        return transformedTriples;
    }

}