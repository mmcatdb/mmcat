package cz.matfyz.transformations.processes;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.utils.Statistics;
import cz.matfyz.core.utils.Statistics.Counter;
import cz.matfyz.core.utils.Statistics.Interval;
import cz.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.matfyz.transformations.algorithms.ICAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InstanceToDatabase {

    private Collection<Mapping> allMappings;
    private InstanceCategory currentInstance;
    private AbstractDDLWrapper ddlWrapper;
    private AbstractDMLWrapper dmlWrapper;
    private AbstractICWrapper icWrapper;

    public InstanceToDatabase input(
        Collection<Mapping> allMappings,
        InstanceCategory currentInstance,
        AbstractDDLWrapper ddlWrapper,
        AbstractDMLWrapper dmlWrapper,
        AbstractICWrapper icWrapper
    ) {
        this.allMappings = allMappings;
        this.currentInstance = currentInstance;
        this.ddlWrapper = ddlWrapper;
        this.dmlWrapper = dmlWrapper;
        this.icWrapper = icWrapper;

        return this;
    }

    public record InstanceToDatabaseResult(
        String statementsAsString,
        Collection<AbstractStatement> statements
    ) {}

    public InstanceToDatabaseResult run() {
        try {
            return innerRun();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private InstanceToDatabaseResult innerRun() {
        if (allMappings.size() == 0)
            return new InstanceToDatabaseResult("", List.of());

        Statistics.start(Interval.INSTANCE_TO_DATABASE);

        final InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceBuilder(allMappings.iterator().next().category()).build();

        final var ddlStatements = new ArrayList<AbstractStatement>();
        final var icStatements = new ArrayList<AbstractStatement>();
        final var dmlStatements = new ArrayList<AbstractStatement>();

        for (final var mapping : allMappings) {
            Statistics.start(Interval.CTM_ALGORIGHM);

            ddlStatements.add(DDLAlgorithm.run(mapping, instance, ddlWrapper));
            icStatements.add(ICAlgorithm.run(mapping, allMappings, icWrapper));
            dmlStatements.addAll(DMLAlgorithm.run(mapping, instance, dmlWrapper));

            Statistics.end(Interval.CTM_ALGORIGHM);
        }

        final var statementsAsString = statementsToString(ddlStatements, icStatements, dmlStatements, dmlWrapper);

        // The mappings can be processed in whatever order. However, the final statements must be:
        //  - All DDL statements.
        //  - All IC statements.
        //  - All DML statements.
        // So that the IC statements can work with tables defined by the DDL statements and so on.

        final var statements = new ArrayList<AbstractStatement>();
        statements.addAll(ddlStatements);
        statements.addAll(icStatements);
        statements.addAll(dmlStatements);

        Statistics.set(Counter.CREATED_STATEMENTS, dmlStatements.size());

        Statistics.end(Interval.INSTANCE_TO_DATABASE);

        return new InstanceToDatabaseResult(statementsAsString, statements);
    }

    private String statementsToString(List<AbstractStatement> ddlStatements, List<AbstractStatement> icStatements, List<AbstractStatement> dmlStatements, AbstractDMLWrapper dmlWrapper) {
        final var output = new StringBuilder();
        for (final var ddlStatement : ddlStatements)
            output.append(ddlStatement.getContent()).append("\n");

        for (final var icStatement : icStatements)
            output.append(icStatement.getContent()).append("\n");

        for (final var dmlStatement : dmlStatements)
            output.append(dmlStatement.getContent()).append("\n");

        return output.toString();
    }

}
