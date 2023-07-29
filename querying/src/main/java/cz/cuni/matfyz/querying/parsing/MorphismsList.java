package cz.cuni.matfyz.querying.parsing;

import java.util.List;

class MorphismsList extends QueryNode {

    @Override MorphismsList asMorphisms() {
        return this;
    }

    public final List<MorphismNode> morphisms;

    public MorphismsList(List<MorphismNode> morphisms) {
        this.morphisms = morphisms;
    }

}