package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.server.entity.database.Database;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record MappingOptionsView(
    int categoryId,
    List<Database> databases
) {}
