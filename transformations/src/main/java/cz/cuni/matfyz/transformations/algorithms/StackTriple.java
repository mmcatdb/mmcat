package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.record.IComplexRecord;
import cz.cuni.matfyz.core.utils.IndentedStringBuilder;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class StackTriple {
    
    public final InstanceMorphism parentToChild;
    public final DomainRow parentRow;
    public final AccessPath childAccessPath;
    public final IComplexRecord parentRecord;
    
    public StackTriple(DomainRow parentRow, InstanceMorphism parentToChild, AccessPath childAccessPath, IComplexRecord parentRecord) {
        this.parentRow = parentRow;
        this.parentToChild = parentToChild;
        this.childAccessPath = childAccessPath;
        this.parentRecord = parentRecord;
    }
    
    @Override
    public String toString() {
        var innerBuilder = new IndentedStringBuilder(1);
        innerBuilder.append("parentToChildMorphism: ").append(parentToChild.signature()).append(",\n");
        innerBuilder.append("parentRow: ").append(parentRow).append(",\n");
        innerBuilder.append("childAccessPath: ").append(childAccessPath).append(",\n");
        innerBuilder.append("record: ").append(parentRecord);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<\n").append(innerBuilder).append(">");
        
        return builder.toString();
    }
}
