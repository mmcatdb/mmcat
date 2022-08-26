package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.record.IComplexRecord;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.utils.IndentedStringBuilder;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class StackTriple {
    
    public final SchemaMorphism parentToChildMorphism;
    public final DomainRow parentRow;
    public final AccessPath parentAccessPath;
    public final IComplexRecord parentRecord;
    
    public StackTriple(DomainRow parentRow, SchemaMorphism parentToChildMorphism, AccessPath parentAccessPath, IComplexRecord parentRecord) {
        this.parentRow = parentRow;
        this.parentToChildMorphism = parentToChildMorphism;
        this.parentAccessPath = parentAccessPath;
        this.parentRecord = parentRecord;
    }
    
    @Override
    public String toString() {
        var innerBuilder = new IndentedStringBuilder(1);
        innerBuilder.append("parentToChildMorphism: ").append(parentToChildMorphism.signature()).append(",\n");
        innerBuilder.append("parentRow: ").append(parentRow).append(",\n");
        innerBuilder.append("parentAccessPath: ").append(parentAccessPath).append(",\n");
        innerBuilder.append("record: ").append(parentRecord);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<\n").append(innerBuilder).append(">");
        
        return builder.toString();
    }
}
