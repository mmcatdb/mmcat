package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.statements.DDLStatement;
import cz.cuni.matfyz.statements.DMLStatement;
import cz.cuni.matfyz.statements.ICStatement;
import cz.cuni.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.cuni.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.cuni.matfyz.transformations.algorithms.ICAlgorithm;

import java.util.List;
import java.util.Map;

/**
 * @author jachym.bartik
 */
public class InstanceToDatabase {

    private Mapping mapping;
    private Map<Name, Mapping> allMappings;
    private InstanceCategory currentInstance;
    private AbstractDDLWrapper ddlWrapper;
    private AbstractPushWrapper pushWrapper;
    private AbstractICWrapper icWrapper;

    public void input(Mapping mapping, Map<Name, Mapping> allMappings, InstanceCategory currentInstance, AbstractDDLWrapper ddlWrapper, AbstractPushWrapper pushWrapper, AbstractICWrapper icWrapper) {
        this.mapping = mapping;
        this.allMappings = allMappings;
        this.currentInstance = currentInstance;
        this.ddlWrapper = ddlWrapper;
        this.pushWrapper = pushWrapper;
        this.icWrapper = icWrapper;
    }
    
    public DataResult<String> run() {
        
        Statistics.start(Interval.INSTANCE_TO_DATABASE);

        final InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        final var ddlTransformation = new DDLAlgorithm();
        ddlTransformation.input(mapping, instance, ddlWrapper);

        final var icTransformation = new ICAlgorithm();
        icTransformation.input(mapping, allMappings, icWrapper);
        
        final var dmlTransformation = new DMLAlgorithm();
        dmlTransformation.input(mapping, instance, pushWrapper);

        Statistics.start(Interval.CTM_ALGORIGHM);
        final DDLStatement ddlStatement = ddlTransformation.algorithm();
        final ICStatement icStatement = icTransformation.algorithm();
        final List<DMLStatement> dmlStatements = dmlTransformation.algorithm();
        Statistics.end(Interval.CTM_ALGORIGHM);

        final var output = new StringBuilder();
        output.append(ddlStatement.getContent())
            .append("\n");

        output.append(icStatement.getContent())
            .append("\n");

        Statistics.set(Counter.CREATED_STATEMENTS, dmlStatements.size());

        for (final DMLStatement dmlStatement : dmlStatements) {
            output.append(dmlStatement.getContent())
                .append("\n");
        }

        Statistics.end(Interval.INSTANCE_TO_DATABASE);

        return new DataResult<>(output.toString());
    }

}
