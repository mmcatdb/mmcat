package cz.matfyz.querying.parsing;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.parsing.Filter.ConditionFilter;
import cz.matfyz.querying.parsing.Filter.ValueFilter;
import cz.matfyz.querying.parsing.Term.Variable;

import java.util.ArrayList;
import java.util.List;

public class WhereClause implements ParserNode {

    public enum Type {
        Where,
        Optional,
        Minus,
        Union,
    }

    public final Type type;
    public final List<WhereClause> nestedClauses;

    /** This is the terms as the user intended. It's a list, because the user can input them this way. */
    public final Term.Builder termBuilder;
    public final List<TermTree<Signature>> originalTermTrees;
    public final TermTree<BaseSignature> termTree;

    public final List<ConditionFilter> conditionFilters;
    public final List<ValueFilter> valueFilters;

    public WhereClause(
        Type type,
        List<WhereClause> nestedClauses,
        Term.Builder termBuilder,
        List<TermTree<Signature>> originalTermTrees,
        List<ConditionFilter> conditionFilters,
        List<ValueFilter> valueFilters
    ) {
        this.type = type;
        this.nestedClauses = nestedClauses;

        this.termBuilder = termBuilder;
        this.originalTermTrees = originalTermTrees;
        this.termTree = createTermTree(originalTermTrees, termBuilder);

        this.conditionFilters = conditionFilters;
        this.valueFilters = valueFilters;
    }

    private static TermTree<BaseSignature> createTermTree(List<TermTree<Signature>> originalTermTrees, Term.Builder termBuilder) {
        final List<TermTree<BaseSignature>> termTrees = originalTermTrees.stream().map(originalTerm -> splitTerm(originalTerm, termBuilder)).toList();

        return TermTree.fromList(termTrees);
    }

    /**
     * For each compound morphism (A) -x/y-> (B), split it by inserting intermediate internal variables in such a way that each triple contains a base morphism only.
     */
    private static TermTree<BaseSignature> splitTerm(TermTree<Signature> originalTerm, Term.Builder termBuilder) {
        final var children = originalTerm.children.stream().map(child -> splitTerm(child, termBuilder)).toList();

        if (originalTerm.edgeFromParent == null) {
            final TermTree<BaseSignature> output = TermTree.root(originalTerm.term.asVariable());
            children.forEach(output::addChild);
            return output;
        }

        // First, we prepare the base signatutres as well as the new terms.
        final var bases = originalTerm.edgeFromParent.toBases();
        final List<Term> terms = new ArrayList<>();

        // First n-1 terms are newly generated. The last one is the original term.
        for (int i = 0; i < bases.size() - 1; i++)
            terms.add(termBuilder.generatedVariable());
        terms.add(originalTerm.term);

        final TermTree<BaseSignature> output = TermTree.child(terms.get(0), bases.get(0));
        TermTree<BaseSignature> current = output;
        for (int i = 1; i < bases.size(); i++) {
            final TermTree<BaseSignature> newTerm = TermTree.child(terms.get(i), bases.get(i));
            current.addChild(newTerm);
            current = newTerm;
        }

        children.forEach(current::addChild);

        return output;
    }

    public static WhereTriple createTriple(Variable parent, BaseSignature edge, Term child) {
        return edge.isDual()
            ? new WhereTriple(child.asVariable(), edge.dual(), parent)
            : new WhereTriple(parent, edge, child);
    }

    public static class WhereTriple implements Edge<Term> {

        public final Variable subject;
        public final BaseSignature signature;
        public final Term object;

        WhereTriple(Variable subject, BaseSignature signature, Term object) {
            this.subject = subject;
            this.signature = signature;
            this.object = object;
        }

        @Override public Term from() {
            return subject;
        }

        @Override public Term to() {
            return object;
        }

        @Override public String toString() {
            return subject.toString() + " " + signature.toString() + " " + object.toString();
        }

    }

}
