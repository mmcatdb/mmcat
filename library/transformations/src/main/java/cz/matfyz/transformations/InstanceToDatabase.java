package cz.matfyz.transformations;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.utils.Statistics;
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

        Statistics.start(RUN_INTERVAL);

        final var ddlStatements = new ArrayList<AbstractStatement>();
        final var icStatements = new ArrayList<AbstractStatement>();
        final var dmlStatements = new ArrayList<AbstractStatement>();

        for (final var mapping : allMappings) {
            Statistics.start(CTM_INTERVAL);

            ddlStatements.add(DDLAlgorithm.run(mapping, currentInstance, ddlWrapper));
            icStatements.addAll(ICAlgorithm.run(mapping, allMappings, icWrapper));
            dmlStatements.addAll(DMLAlgorithm.run(mapping, currentInstance, dmlWrapper));

            Statistics.end(CTM_INTERVAL);
        }

        final var sortedIcStatements = AbstractStatement.sortByPriority(icStatements);

        // The mappings can be processed in whatever order. However, the final statements must be:
        //  - All DDL statements.
        //  - All DML statements.
        //  - All IC statements.
        // So that the IC statements can work with tables defined by the DDL statements and there are no FK constraints violations in the DML statements.
        // Also, there might be several dependency groups of IC statements, so they must be sorted by priority.

        final var statements = new ArrayList<AbstractStatement>();
        statements.addAll(ddlStatements);
        statements.addAll(dmlStatements);
        statements.addAll(sortedIcStatements);

        Statistics.set(STATEMENTS_COUNTER, dmlStatements.size());
        Statistics.end(RUN_INTERVAL);

        final var statementsAsString = statementsToString(statements, dmlWrapper);

        return new InstanceToDatabaseResult(statementsAsString, statements);
    }

    private String statementsToString(Collection<AbstractStatement> statements, AbstractDMLWrapper dmlWrapper) {
        final var sb = new StringBuilder();
        for (final var ddlStatement : statements)
            sb.append(ddlStatement.getContent()).append("\n");

        return sb.toString();
    }

    public static final String RUN_INTERVAL = "instance-to-database";
    public static final String CTM_INTERVAL = "ctm-algorithm";
    public static final String STATEMENTS_COUNTER = "created-statements";

}
