package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory.InstancePath;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.record.IComplexRecord;
import cz.matfyz.core.utils.IndentedStringBuilder;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class StackTriple {
    
    public final InstancePath parentToChild;
    public final DomainRow parentRow;
    public final AccessPath childAccessPath;
    public final IComplexRecord parentRecord;
    
    public StackTriple(DomainRow parentRow, InstancePath parentToChild, AccessPath childAccessPath, IComplexRecord parentRecord) {
        this.parentRow = parentRow;
        this.parentToChild = parentToChild;
        this.childAccessPath = childAccessPath;
        this.parentRecord = parentRecord;
    }
    
    @Override
    public String toString() {
        var innerBuilder = new IndentedStringBuilder(1);
        innerBuilder.append("parentToChildPath: ").append(parentToChild.signature()).append(",\n");
        innerBuilder.append("parentRow: ").append(parentRow).append(",\n");
        innerBuilder.append("childAccessPath: ").append(childAccessPath).append(",\n");
        innerBuilder.append("record: ").append(parentRecord);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<\n").append(innerBuilder).append(">");
        
        return builder.toString();
    }
}
