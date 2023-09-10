package cz.matfyz.querying.parsing;

import java.util.List;

class ObjectsList extends ParserNode {

    @Override ObjectsList asObjectsList() {
        return this;
    }

    public final List<ValueNode> objects;

    public ObjectsList(List<ValueNode> objects) {
        this.objects = objects;
    }

}