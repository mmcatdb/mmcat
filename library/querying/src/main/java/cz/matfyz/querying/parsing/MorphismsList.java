package cz.matfyz.querying.parsing;

import java.util.List;

class MorphismsList extends ParserNode {

    @Override MorphismsList asMorphisms() {
        return this;
    }

    public final List<MorphismNode> morphisms;

    public MorphismsList(List<MorphismNode> morphisms) {
        this.morphisms = morphisms;
    }

}