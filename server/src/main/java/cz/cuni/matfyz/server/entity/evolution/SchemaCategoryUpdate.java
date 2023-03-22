package cz.cuni.matfyz.server.entity.evolution;

import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record SchemaCategoryUpdate(
    Version beforeVersion,
    List<SchemaModificationOperation> operations
) {

    public cz.cuni.matfyz.evolution.schema.SchemaCategoryUpdate toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.SchemaCategoryUpdate(
            beforeVersion,
            operations.stream().map(operation -> operation.toEvolution(context)).toList()
        );
    }

}
